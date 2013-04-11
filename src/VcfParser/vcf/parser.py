import collections
import re
import csv
import gzip
import sys
import itertools
import codecs
import mysql.connector
import datetime

try:
    from collections import OrderedDict
except ImportError:
    from ordereddict import OrderedDict

try:
    import pysam
except ImportError:
    pysam = None

try:
    import cparse
except ImportError:
    cparse = None

from model import _Call, _Record, make_calldata_tuple
from model import _Substitution, _Breakend, _SingleBreakend, _SV
from DatabaseConnection import DatabaseConnection


# Metadata parsers/constants
RESERVED_INFO = {
    'AA': 'String', 'AC': 'Integer', 'AF': 'Float', 'AN': 'Integer',
    'BQ': 'Float', 'CIGAR': 'String', 'DB': 'Flag', 'DP': 'Integer',
    'END': 'Integer', 'H2': 'Flag', 'MQ': 'Float', 'MQ0': 'Integer',
    'NS': 'Integer', 'SB': 'String', 'SOMATIC': 'Flag', 'VALIDATED': 'Flag',

    # VCF 4.1 Additions
    'IMPRECISE':'Flag', 'NOVEL':'Flag', 'END':'Integer', 'SVTYPE':'String',
    'CIPOS':'Integer','CIEND':'Integer','HOMLEN':'Integer','HOMSEQ':'Integer',
    'BKPTID':'String','MEINFO':'String','METRANS':'String','DGVID':'String',
    'DBVARID':'String','MATEID':'String','PARID':'String','EVENT':'String',
    'CILEN':'Integer','CN':'Integer','CNADJ':'Integer','CICN':'Integer',
    'CICNADJ':'Integer'
}

RESERVED_FORMAT = {
    'GT': 'String', 'DP': 'Integer', 'FT': 'String', 'GL': 'Float',
    'GQ': 'Float', 'HQ': 'Float',

    # VCF 4.1 Additions
    'CN':'Integer','CNQ':'Float','CNL':'Float','NQ':'Integer','HAP':'Integer',
    'AHAP':'Integer'
}

# Spec is a bit weak on which metadata lines are singular, like fileformat
# and which can have repeats, like contig
SINGULAR_METADATA = ['fileformat', 'fileDate', 'reference']

# Conversion between value in file and Python value
field_counts = {
    '.': None,  # Unknown number of values
    'A': -1,  # Equal to the number of alleles in a given record
    'G': -2,  # Equal to the number of genotypes in a given record
}


_Info = collections.namedtuple('Info', ['id', 'num', 'type', 'desc'])
_Filter = collections.namedtuple('Filter', ['id', 'desc'])
_Alt = collections.namedtuple('Alt', ['id', 'desc'])
_Format = collections.namedtuple('Format', ['id', 'num', 'type', 'desc'])
_SampleInfo = collections.namedtuple('SampleInfo', ['samples', 'gt_bases', 'gt_types', 'gt_phases'])


class _vcf_metadata_parser(object):
    '''Parse the metadat in the header of a VCF file.'''
    def __init__(self):
        super(_vcf_metadata_parser, self).__init__()
        self.info_pattern = re.compile(r'''\#\#INFO=<
            ID=(?P<id>[^,]+),
            Number=(?P<number>-?\d+|\.|[AG]),
            Type=(?P<type>Integer|Float|Flag|Character|String),
            Description="(?P<desc>[^"]*)"
            >''', re.VERBOSE)
        self.filter_pattern = re.compile(r'''\#\#FILTER=<
            ID=(?P<id>[^,]+),
            Description="(?P<desc>[^"]*)"
            >''', re.VERBOSE)
        self.alt_pattern = re.compile(r'''\#\#ALT=<
            ID=(?P<id>[^,]+),
            Description="(?P<desc>[^"]*)"
            >''', re.VERBOSE)
        self.format_pattern = re.compile(r'''\#\#FORMAT=<
            ID=(?P<id>.+),
            Number=(?P<number>-?\d+|\.|[AG]),
            Type=(?P<type>.+),
            Description="(?P<desc>.*)"
            >''', re.VERBOSE)
        self.meta_pattern = re.compile(r'''##(?P<key>.+?)=(?P<val>.+)''')

    def vcf_field_count(self, num_str):
        """Cast vcf header numbers to integer or None"""
        if num_str not in field_counts:
            # Fixed, specified number
            return int(num_str)
        else:
            return field_counts[num_str]

    def read_info(self, info_string):
        '''Read a meta-information INFO line.'''
        match = self.info_pattern.match(info_string)
        if not match:
            raise SyntaxError(
                "One of the INFO lines is malformed: %s" % info_string)

        num = self.vcf_field_count(match.group('number'))
        info = _Info(match.group('id'), num,
                     match.group('type'), match.group('desc'))

        return (match.group('id'), info)

    def read_filter(self, filter_string):
        '''Read a meta-information FILTER line.'''
        match = self.filter_pattern.match(filter_string)
        if not match:
            raise SyntaxError(
                "One of the FILTER lines is malformed: %s" % filter_string)

        filt = _Filter(match.group('id'), match.group('desc'))

        return (match.group('id'), filt)

    def read_alt(self, alt_string):
        '''Read a meta-information ALTline.'''
        match = self.alt_pattern.match(alt_string)
        if not match:
            raise SyntaxError(
                "One of the FILTER lines is malformed: %s" % alt_string)

        alt = _Alt(match.group('id'), match.group('desc'))

        return (match.group('id'), alt)

    def read_format(self, format_string):
        '''Read a meta-information FORMAT line.'''
        match = self.format_pattern.match(format_string)
        if not match:
            raise SyntaxError(
                "One of the FORMAT lines is malformed: %s" % format_string)

        num = self.vcf_field_count(match.group('number'))
        form = _Format(match.group('id'), num,
                       match.group('type'), match.group('desc'))

        return (match.group('id'), form)

    def read_meta_hash(self, meta_string):
        items = re.split("[<>]", meta_string)
        # Removing initial hash marks and final equal sign
        key = items[0][2:-1]
        hashItems = items[1].split(',')
        val = dict(item.split("=") for item in hashItems)
        return key, val

    def read_meta(self, meta_string):
        if re.match("##.+=<", meta_string):
            return self.read_meta_hash(meta_string)
        else:
            match = self.meta_pattern.match(meta_string)
            return match.group('key'), match.group('val')


class Reader(object):
    """ Reader for a VCF v 4.0 file, an iterator returning ``_Record objects`` """

    def __init__(self, fsock=None, filename=None, compressed=False, prepend_chr=False, vcfName = None):
        """ Create a new Reader for a VCF file.

            You must specify either fsock (stream) or filename.  Gzipped streams
            or files are attempted to be recogized by the file extension, or gzipped
            can be forced with ``compressed=True``
        """
        super(Reader, self).__init__()
        
        self.db = DatabaseConnection()
        if vcfName == None:
            now = datetime.datetime.now()
            vcfName = now.strftime("%Y-%m-%d_%H:%M")
        self.vcfId = self.db.createVcf( vcfName )
        
        #add vcf header
        self.vcfHeader = "";
        
        if not (fsock or filename):
            raise Exception('You must provide at least fsock or filename')

        if fsock:
            self.reader = fsock
            if filename is None and hasattr(fsock, 'name'):
                filename = fsock.name
                compressed = compressed or filename.endswith('.gz')
        elif filename:
            compressed = compressed or filename.endswith('.gz')
            self.reader = open(filename, 'rb' if compressed else 'rt')
        self.filename = filename
        if compressed:
            self.reader = gzip.GzipFile(fileobj=self.reader)
            if sys.version > '3':
                self.reader = codecs.getreader('ascii')(self.reader)
        
        #: metadata fields from header (string or hash, depending)
        self.metadata = None
        #: INFO fields from header
        self.infos = None
        #: FILTER fields from header
        self.filters = None
        #: ALT fields from header
        self.alts = None
        #: FORMAT fields from header
        self.formats = None
        self.samples = None
        self._sample_indexes = None
        #self._header_lines = []
        self._tabix = None
        self._prepend_chr = prepend_chr
        self._parse_metainfo()
        self._format_cache = {}
        
    def __iter__(self):
        return self

    def _parse_metainfo(self):
        '''Parse the information stored in the metainfo of the VCF.

        The end user shouldn't have to use this.  She can access the metainfo
        directly with ``self.metadata``.'''
        for attr in ('metadata', 'infos', 'filters', 'alts', 'formats'):
            setattr(self, attr, OrderedDict())

        parser = _vcf_metadata_parser()
        line = self.reader.next()
        while line.startswith('##'):
            self.vcfHeader += line;
            #self._header_lines.append(line)
            line = line.strip()

            if line.startswith('##INFO'):
                key, val = parser.read_info(line)
                #self.infos[key] = val
                self.db.handleInfo(key, val)
                '''
            elif line.startswith('##FILTER'):
                key, val = parser.read_filter(line)
                #self.filters[key] = val

            elif line.startswith('##ALT'):
                key, val = parser.read_alt(line)
                #self.alts[key] = val
                '''
            elif line.startswith('##FORMAT'):
                key, val = parser.read_format(line)
                #self.formats[key] = val
                self.db.handleFormat(key, val)
                '''
            else:
                key, val = parser.read_meta(line.strip())
                if key in SINGULAR_METADATA:
                    self.metadata[key] = val
                else:
                    if key not in self.metadata:
                        self.metadata[key] = []
                    self.metadata[key].append(val)
                '''
            line = self.reader.next()

        #get last header line
        self.vcfHeader += line;
            
        self.db.createVcfHeader( self.vcfId, self.vcfHeader )
            
        fields = re.split('\t| +', line.rstrip())
        self.samples = fields[9:]
        
        self.db.createVcfIndividuals( self.vcfId, fields[9:] )
        
        self._sample_indexes = dict([(x,i) for (i,x) in enumerate(self.samples)])
        

    def _map(self, func, iterable, bad='.'):
        '''``map``, but make bad values None.'''
        return [func(x) if x != bad else None
                for x in iterable]

    def _parse_info(self, info_str, entryDbId):
        if info_str == '.':
            return {}

        entries = info_str.split(';')

        for entry in entries:
            entry = entry.split('=') 
            self.db.insertInfo(entryDbId, entry)

    def _parse_sample_format(self, samp_fmt):
        """ Parse the format of the calls in this _Record """
        samp_fmt = make_calldata_tuple(samp_fmt.split(':'))

        for fmt in samp_fmt._fields:
            try:
                entry_type = self.formats[fmt].type
                entry_num = self.formats[fmt].num
            except KeyError:
                entry_num = None
                try:
                    entry_type = RESERVED_FORMAT[fmt]
                except KeyError:
                    entry_type = 'String'
            samp_fmt._types.append(entry_type)
            samp_fmt._nums.append(entry_num)
        return samp_fmt

    def _parse_samples(self, samples, samp_fmt, EntryDbID):
        '''Parse a sample entry according to the format specified in the FORMAT
        column.

        NOTE: this method has a cython equivalent and care must be taken
        to keep the two methods equivalent
        '''
        # TODO 1 remove print when ready
        print samp_fmt
        individGeno = samp_fmt.split(":")
        IndividualFunctions = []
        CustomGeno = []
		
        for genotype in individGeno:
            if ( genotype == "AD" ):
                IndividualFunctions.append(self.db.createAD)
            elif (genotype == "DP" ):
                 IndividualFunctions.append(self.db.createDP)
            elif (genotype == "EC" ):
                IndividualFunctions.append(self.db.createEC)
            elif (genotype == "FT" ):
                IndividualFunctions.append(self.db.createFT)
            elif (genotype == "GL" ):
                IndividualFunctions.append(self.db.createGL)
            elif (genotype == "GLE" ):      
                 IndividualFunctions.append(self.db.createGLE)
            elif (genotype == "GP" ):
                IndividualFunctions.append(self.db.createGP)
            elif (genotype == "GQ" ):
                IndividualFunctions.append(self.db.createGQ)
            elif (genotype == "GT" ):
                IndividualFunctions.append(self.db.createGT)
            elif (genotype == "HQ" ):
                IndividualFunctions.append(self.db.createHQ)
            elif (genotype == "PL" ):      
                 IndividualFunctions.append(self.db.createPL)
            elif (genotype == "PQ" ):
                IndividualFunctions.append(self.db.createPQ)
            elif (genotype == "PS" ):
                IndividualFunctions.append(self.db.createPS)
            else:
                CustomGeno.append( genotype )
                IndividualFunctions.append(self.db.createIndividualDefault)
           
        
        if samp_fmt not in self._format_cache:
            self._format_cache[samp_fmt] = self._parse_sample_format(samp_fmt)

        samp_fmt = self._format_cache[samp_fmt]

        if cparse:
            return cparse.parse_samples(
                self.samples, samples, samp_fmt, samp_fmt._types, samp_fmt._nums, site)

        samp_data = []
        _map = self._map

        nfields = len(samp_fmt._fields)
        
        indNumber = 0;
        indId = 0

        for name, sample in itertools.izip(self.samples, samples):
            
            customCount = 0
            
            indId = self.db.createIndividualEntry( EntryDbID, indNumber ); 
            if indId == -1:
                print "Failed to create individual entry"
                
            indNumber += 1
            # parse the data for this sample
            sampdat = [None] * nfields

            for i, vals in enumerate(sample.split(':')):
                # short circuit the most common
                if ( IndividualFunctions[i] == self.db.createIndividualDefault ):
                    IndividualFunctions[i]( CustomGeno[customCount], indId, vals )
                    customCount += 1
                else:
                    IndividualFunctions[i]( indId, vals ) 
                
                
                if vals == '.' or vals == './.':
                    sampdat[i] = None
                    continue

                entry_num = samp_fmt._nums[i]
                entry_type = samp_fmt._types[i]

                # we don't need to split single entries
                if entry_num == 1 or ',' not in vals:

                    #TODO: add DB upload and subroutines
                    if entry_type == 'Integer':
                        sampdat[i] = int(vals)
                    elif entry_type == 'Float':
                        sampdat[i] = float(vals)
                    else:
                        sampdat[i] = vals

                    if entry_num != 1:
                        sampdat[i] = (sampdat[i])

                    continue

                vals = vals.split(',')

                if entry_type == 'Integer':
                    sampdat[i] = _map(int, vals)
                elif entry_type == 'Float' or entry_type == 'Numeric':
                    sampdat[i] = _map(float, vals)
                else:
                    sampdat[i] = vals

            # create a call object
            #call = _Call(site, name, samp_fmt(*sampdat))
            #samp_data.append(call)

        #return samp_data

    def _parse_alt(self, str):
        if re.search('[\[\]]', str) is not None:
            # Paired breakend
            items = re.split('[\[\]]', str)
            remoteCoords = items[1].split(':')
            chr = remoteCoords[0]
            if chr[0] == '<':
                chr = chr[1:-1]
                withinMainAssembly = False
            else:
                withinMainAssembly = True
            pos = remoteCoords[1]
            orientation = (str[0] == '[' or str[0] == ']')
            remoteOrientation = (re.search('\[', str) is not None)
            if orientation:
                connectingSequence = items[2]
            else:
                connectingSequence = items[0]
            return _Breakend(chr, pos, orientation, remoteOrientation, connectingSequence, withinMainAssembly)
        elif str[0] == '.' and len(str) > 1:
            return _SingleBreakend(True, str[1:])
        elif str[-1] == '.' and len(str) > 1:
            return _SingleBreakend(False, str[:-1])
        elif str[0] == "<" and str[-1] == ">":
            return _SV(str[1:-1])
        else:
            return _Substitution(str)

    def next(self):
        '''Return the next record in the file.'''
        try:
            line = self.reader.next()
        except:
            return True

        row = re.split('\t| +', line.strip())
        
        chrom = row[0]
        if self._prepend_chr:
            chrom = 'chr' + chrom
        pos = int(row[1])

        queryId = row[2]
        if row[2] != '.':
            ID = row[2]
        else:
            ID = None

        ref = row[3]
        queryAlt = row[4]
        alt = self._map(self._parse_alt, row[4].split(','))

        try:
            qual = int(row[5])
        except ValueError:
            try:
                qual = float(row[5])
            except ValueError:
                qual = None

        filt = row[6].split(';') if ';' in row[6] else row[6]
        if filt == 'PASS':
            filt = None

        try:
            fmt = row[8]
        except IndexError:
            fmt = None
        
        entryDbId = self.db.createEntry( self.vcfId, chrom, pos, queryId, ref, queryAlt, qual, filt, fmt)
            
        self._parse_info(row[7], entryDbId)

        #record = _Record(chrom, pos, ID, ref, alt, qual, filt,
        #        info, fmt, self._sample_indexes)

        if fmt is not None:
            self._parse_samples(row[9:], fmt, entryDbId)
            #record.samples = samples
            
        return False

    def fetch(self, chrom, start, end=None):
        """ fetch records from a Tabix indexed VCF, requires pysam
            if start and end are specified, return iterator over positions
            if end not specified, return individual ``_Call`` at start or None
        """
        if not pysam:
            raise Exception('pysam not available, try "pip install pysam"?')

        if not self.filename:
            raise Exception('Please provide a filename (or a "normal" fsock)')

        if not self._tabix:
            self._tabix = pysam.Tabixfile(self.filename)

        if self._prepend_chr and chrom[:3] == 'chr':
            chrom = chrom[3:]

        # not sure why tabix needs position -1
        start = start - 1

        if end is None:
            self.reader = self._tabix.fetch(chrom, start, start + 1)
            try:
                return self.next()
            except StopIteration:
                return None

        self.reader = self._tabix.fetch(chrom, start, end)
        return self



# backwards compatibility
VCFReader = Reader
VCFWriter = Writer
