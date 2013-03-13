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
'''
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
'''
    def __iter__(self):
        return self

    def _parse_metainfo(self):
        '''Parse the information stored in the metainfo of the VCF.

        The end user shouldn't have to use this.  She can access the metainfo
        directly with ``self.metadata``.'''
        for attr in ('metadata', 'infos', 'filters', 'alts', 'formats'):
            setattr(self, attr, OrderedDict())

        parser = _vcf_metadata_parser()
        #TODO: add DB upload
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
                #TODO Julia db connect
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

        fields = re.split('\t| +', line.rstrip())
        self.samples = fields[9:]
        
        self.db.createVcfIndividuals( self.vcfId, fields[9:] )
        
        self._sample_indexes = dict([(x,i) for (i,x) in enumerate(self.samples)])
        

    def _map(self, func, iterable, bad='.'):
        '''``map``, but make bad values None.'''
        return [func(x) if x != bad else None
                for x in iterable]

    def _parse_info(self, info_str, entryDbId):
        '''Parse the INFO field of a VCF entry into a dictionary of Python
        types.

        '''
        
        if info_str == '.':
            return {}

        entries = info_str.split(';')
        #retdict = OrderedDict()

        for entry in entries:
            entry = entry.split('=')
'''
            ID = entry[0]
            #TODO: add DB upload and long if else for options
            try:
                entry_type = self.infos[ID].type
            except KeyError:
                try:
                    entry_type = RESERVED_INFO[ID]
                except KeyError:
                    if entry[1:]:
                        entry_type = 'String'
                    else:
                        entry_type = 'Flag'
'''    
            self.db.insertInfo(entryDbId, entry)
            '''
            if entry_type == 'Integer':
                vals = entry[1].split(',')
                val = self._map(int, vals)
            elif entry_type == 'Float':
                vals = entry[1].split(',')
                val = self._map(float, vals)
            elif entry_type == 'Flag':
                val = True
            elif entry_type == 'String':
                try:
                    val = entry[1]
                except IndexError:
                    val = True

            try:
                if val != True and self.infos[ID].num == 1 and entry_type != 'String':
                    val = val[0]
            except KeyError:
                pass

            retdict[ID] = val

        return retdict
        '''

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

        # check whether we already know how to parse this format
        # TODO at some point add DB
        # TODO 1 remove print when ready
        print samp_fmt
        individGeno = samp_fmt.split(":")
        IndividualFunctions = []
        CustomGeno = []
        #Supported
        #TODO individual
        #JULIA: AD DP, GLE, GL, EC GP, GT, FT, PL, GQ, HQ, PS, PQ        
        
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
                #TODO individ here
                # short circuit the most common
                #MINE
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


class Writer(object):
    """ VCF Writer """

    fixed_fields = "#CHROM POS ID REF ALT QUAL FILTER INFO FORMAT".split()

    # Reverse keys and values in header field count dictionary
    counts = dict((v,k) for k,v in field_counts.iteritems())

    def __init__(self, stream, template, lineterminator="\r\n"):
        self.writer = csv.writer(stream, delimiter="\t", lineterminator=lineterminator)
        self.template = template
        self.stream = stream

        two = '##{key}=<ID={0},Description="{1}">\n'
        four = '##{key}=<ID={0},Number={num},Type={2},Description="{3}">\n'
        _num = self._fix_field_count
        for (key, vals) in template.metadata.iteritems():
            if key in SINGULAR_METADATA:
                vals = [vals]
            for val in vals:
                stream.write('##{0}={1}\n'.format(key, val))
        for line in template.infos.itervalues():
            stream.write(four.format(key="INFO", *line, num=_num(line.num)))
        for line in template.formats.itervalues():
            stream.write(four.format(key="FORMAT", *line, num=_num(line.num)))
        for line in template.filters.itervalues():
            stream.write(two.format(key="FILTER", *line))
        for line in template.alts.itervalues():
            stream.write(two.format(key="ALT", *line))

        self._write_header()

    def _write_header(self):
        # write INFO, etc
        self.writer.writerow(self.fixed_fields + self.template.samples)

    def write_record(self, record):
        """ write a record to the file """
        ffs = self._map(str, [record.CHROM, record.POS, record.ID, record.REF]) \
              + [self._format_alt(record.ALT), record.QUAL or '.', self._format_filter(record.FILTER),
                 self._format_info(record.INFO), record.FORMAT]

        samples = [self._format_sample(record.FORMAT, sample)
            for sample in record.samples]
        self.writer.writerow(ffs + samples)

    def flush(self):
        """Flush the writer"""
        try:
            self.stream.flush()
        except AttributeError:
            pass

    def close(self):
        """Close the writer"""
        try:
            self.stream.close()
        except AttributeError:
            pass

    def _fix_field_count(self, num_str):
        """Restore header number to original state"""
        if num_str not in self.counts:
            return num_str
        else:
            return self.counts[num_str]

    def _format_alt(self, alt):
        return ','.join(self._map(str, alt))

    def _format_filter(self, flt):
        return self._stringify(flt, none='PASS', delim=';')

    def _format_info(self, info):
        if not info:
            return '.'
        return ';'.join([self._stringify_pair(x,y) for x, y in info.iteritems()])

    def _format_sample(self, fmt, sample):
        if sample.data.GT is None:
            return "./."
        return ':'.join([self._stringify(x) for x in sample.data])

    def _stringify(self, x, none='.', delim=','):
        if type(x) == type([]):
            return delim.join(self._map(str, x, none))
        return str(x) if x is not None else none

    def _stringify_pair(self, x, y, none='.', delim=','):
        if isinstance(y, bool):
            return str(x) if y else ""
        return "%s=%s" % (str(x), self._stringify(y, none=none, delim=delim))

    def _map(self, func, iterable, none='.'):
        '''``map``, but make None values none.'''
        return [func(x) if x is not None else none
                for x in iterable]

class DatabaseConnection():

    def __init__(self):
        #Connect to database
        self.cnx = mysql.connector.connect(user = 'vcf_user', password = 'vcf', host = 'localhost', database = 'vcf_analyzer', buffered=True)
        self.cursor = self.cnx.cursor()
        
    def dbClose(self):
        self.cursor.close()
        self.cnx.close()
        
    def handleInfo(self, infoName, infoData):
        query = ("SELECT count(*) FROM InfoTable WHERE InfoName='" + infoName + "'")
        self.cursor.execute(query)
        count = self.cursor.fetchone()[0]
        if (count == 0):
            dataType = None
            typeEnum = 0;
            typeEnum, dataType = stringToTypeEnum( infoData.type )    
            
            rowCount = 1
            print ">>>>>",
            print infoData
            if (infoData.type == "Flag"):
                rowCount = 0;
            elif ( infoData.num != None ):
                print ">>>>>",
                print infoData.num
                rowCount = int(infoData.num)
                
            createQuery = ("CREATE TABLE `{}` ( EntryId BIGINT NOT NULL PRIMARY KEY").format( infoName )
            for i in range(rowCount):
                createQuery += (", `val{}` {} DEFAULT NULL").format( i, dataType )
            createQuery += ")"
            
            constraint = ("ALTER TABLE  `{}` ADD FOREIGN KEY ( `EntryId` ) ").format(infoName)
            constraint += " REFERENCES `vcf_analyzer`.`VcfEntry` (`EntryId`) ON DELETE CASCADE ON UPDATE CASCADE"
            
            infoQuery = ("INSERT INTO `vcf_analyzer`.`InfoTable` (`InfoName`, `Type`, `Count`, `Description`) " +
                        "VALUES ( '{}', '{}', '{}', '{}' )").format(
                        infoName, typeEnum, rowCount, infoData.desc[:150] )
            try:
                self.cursor.execute(createQuery)
                self.cursor.execute(constraint)
                self.cursor.execute(infoQuery)
                self.cnx.commit()
                
            except:
                self.cnx.rollback()
                return -1

    def insertInfo(self, entryDbId, entry):
        
        if (len(entry) == 2):
            query = "INSERT INTO `vcf_analyzer`.`{}` VALUES ('{}', '{}')".format(entry[0], entryDbId, entry[1])
        else:
            query = "INSERT INTO `vcf_analyzer`.`{}` VALUES ('{}')".format(entry[0], entryDbId)
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        except:
            print "Fatal Error"
            return -1

    def handleFormat(self, formatName, formatData):
    
        query = ("SELECT count(*) FROM GenotypeTable WHERE GenoName='" + formatName + "'")
        self.cursor.execute(query)
        count = self.cursor.fetchone()[0]
        if (count == 0):
        
            if ( len(formatName) > 5 ):
                #Name too long
                return -1
                
            dataType = None
            typeEnum = 0;
            rowCount, dataType = stringToTypeEnum( formatData.type )
            
            rowCount = 1
            if ( formatData.num == None and formatData.type == "Flag"):
                rowCount = 0;
                typeEnum = 2
            elif ( formatData.num != None ):
                rowCount = int(formatData.num)
                
            createQuery = ("CREATE TABLE `{}` ( IndID BIGINT NOT NULL PRIMARY KEY").format( formatName )
            for i in range(rowCount):
                createQuery += (", `val{}` {}").format( i, dataType )
            createQuery += ")"
            
            constraint = ("ALTER TABLE  `{}` ADD FOREIGN KEY ( `IndID` ) ").format(formatName)
            constraint += " REFERENCES `vcf_analyzer`.`IndividualEntry` (`IndID`) ON DELETE CASCADE ON UPDATE CASCADE"
            
            infoQuery = ("INSERT INTO `vcf_analyzer`.`GenotypeTable` (`GenoName`, `Type`, `Count`, `Description`) " +
                        "VALUES ( '{}', '{}', '{}', '{}' )").format(
                        formatName, typeEnum, rowCount, formatData.desc[:150] )
            try:
                self.cursor.execute(createQuery)
                self.cursor.execute(constraint)
                self.cursor.execute(infoQuery)
                self.cnx.commit()
                
            except:
                self.cnx.rollback()
                return -1
                
            
    def createEntry(self, vcfId, chrom, pos, id, ref, alt, qual, filter, format):
        query = ("INSERT INTO `vcf_analyzer`.`VcfEntry` (`EntryId`, `VcfId`, `Chrom`, `Pos`, `Id`, `Ref`, `Alt`, `Qual`, `Filter`, `Format`) " +
                "VALUES ( NULL, '{}', '{}', '{}', '{}', '{}', '{}', '{}', '{}', '{}')").format(
                vcfId, chrom, pos, id, ref, alt, qual, filter, format )
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        
            query = ("SELECT max(EntryId) FROM VcfEntry ")
            self.cursor.execute(query)
            return self.cursor.fetchone()[0]
        except:
            self.cnx.rollback()
            return -1

    def createVcfIndividuals(self, vcfId, names ):
    
        queries = []
        indNo = 0
        for name in names:
            queries.append( ("INSERT INTO `vcf_analyzer`.`VcfIndividual` ( `VcfId`, `IndNoVcf`, `IndName`) " +
                            "VALUES ( '{}', '{}', '{}' )").format(
                            vcfId, indNo, name[:25] ) )
            indNo += 1
        
        try:
            for q in queries:
                self.cursor.execute(q)
            self.cnx.commit()
            return indNo
        except:
            self.cnx.rollback()
            return -1
            
    def createIndividualEntry(self, entryId, indNo ):
        query = ("INSERT INTO `vcf_analyzer`.`IndividualEntry` ( `IndID`, `EntryId`, `IndNoVcf`) " +
                "VALUES ( NULL, '{}', '{}' )").format(
                entryId, indNo )
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        
            query2 = ("SELECT max(IndID) FROM IndividualEntry ")
            self.cursor.execute(query2)
            return self.cursor.fetchone()[0]
        except:
            self.cnx.rollback()
            return -1
            
    def createVcf( self, name ):
        
        query = ("SELECT VcfName FROM Vcf ORDER BY VcfId DESC")
        first = ""
        
        try:
            self.cursor.execute(query)
            first = self.cursor.fetchone()
        except:
            self.cnx.rollback()
            return -1   
            
        lastName = None
        if ( len(first) > 0 ):
            lastName = first[0]
        if ( lastName != None ):
            if ( name in lastName):
                if ( name == lastName ):
                    name = name + "-1"
                else:
                    name = lastName[:-1] + str( int(lastName[-1]) + 1 )

        query = ("INSERT INTO `vcf_analyzer`.`Vcf` (`VcfId`, `VcfName`) " +
                "VALUES ( NULL, '{}')").format(
                name[:75] )

        try:
            self.cursor.execute(query)
            self.cnx.commit()
        
            query = ("SELECT max(VcfId) FROM Vcf")
            self.cursor.execute(query)
            return self.cursor.fetchone()[0]
        except:
            self.cnx.rollback()
            return -1
            
    def createDP( self, indId, dpStr ):
    
        if (dpStr == "."):
            dpStr = "NULL"
            
        query = ("INSERT INTO `vcf_analyzer`.`DP` (`IndID`, `ReadDepth`) " +
                "VALUES ( '{}', '{}' )").format(
                indId, dpStr )
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        except:
            self.cnx.rollback()
            return -1
            
    def createGL( self, indId, glStr ):
    
        query = ""
        if ( glStr == "." ):
            query = "INSERT INTO `vcf_analyzer`.`GL` (`IndID`, `AA`, `AB`, `BB`) " + "VALUES ( NULL, NULL, NULL, NULL' )"
        else:
            entries = glStr.split(',')
            for i in range(len(entries)):
                if ( entries[i] == "." ):
                    entries[i] = "NULL"

            if ( len(entries) == 3 ):
                query = ("INSERT INTO `vcf_analyzer`.`GL` (`IndID`, `AA`, `AB`, `BB`) " +
                        "VALUES ( '{}', '{}', '{}', '{}' )").format(
                        indId, entries[0], entries[1], entries[2] )
            elif (len(entries) == 6 ):
                query = ("INSERT INTO `vcf_analyzer`.`GL` (`IndID`, `AA`, `AB`, `BB`, `AC`, `BC`, `CC`) " +
                        "VALUES ( '{}', '{}', '{}', '{}', '{}', '{}', '{}' )").format(
                        indId, entries[0], entries[1], entries[2], entries[3], entries[4], entries[5] )
            else:
                #invalid number of GL values
                return -1
            
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        except:
            self.cnx.rollback()
            return -1
            
    def createGT(self, indID, gtStr):
        gtStrList = list(gtStr)
        outList = []
        n = len(gtStr)

        for i in range(n):
            if (gtStrList[i] == '0'):
                outList.append('0')
            elif (gtStrList[i] == '1'):
                outList.append('1')
            elif (gtStrList[i] == '/'):
                outList.append('0')
            elif (gtStrList[i] == '|'):
                outList.append('1')
            elif (gtStrList[i] == '.'):
                outList.append("NULL")
                
        if (n == 1):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ('{}', '{}', NULL, NULL, NULL, NULL)".format(indID, outList[0])
        elif (n == 2):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ('{}', '{}', '{}', NULL, NULL, NULL)".format(indID, outList[0], outList[1])
        elif (n == 3):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ('{}', '{}', '{}', '{}', NULL, NULL)".format(indID, outList[0], outList[1], outList[2])
        elif (n == 4):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ('{}', '{}', '{}', '{}', '{}', NULL)".format(indID, outList[0], outList[1], outList[2], outList[3])
        elif (n == 5):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ('{}', '{}', '{}', '{}', '{}', '{}')".format(indID, outList[0], outList[1], outList[2], outList[3], outList[4])
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        except:
            self.cnx.rollback()
            return -1
        
        return
        
    def createFT(self, indID, ftStr):
        query = "INSERT INTO `vcf_analyzer`.`FT` VALUES ('{}', '{}')".format(indID, ftStr)
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        except:
            self.cnx.rollback()
            return -1
            
    def createPL(self, indId, plStr):
        if (plStr == "."):
            return #not sure what to do about this case
        else:
            values = plStr.split(',')
            
            if (len(values) == 1):
                query = "INSERT INTO `vcf_analyzer`.`PL` VALUES ('{}', '{}', NULL, NULL, NULL)".format(indId, values[0])
            elif (len(values) == 2):
                query = "INSERT INTO `vcf_analyzer`.`PL` VALUES ('{}', '{}', '{}', NULL, NULL)".format(indId, values[0], values[1])
            elif (len(values) == 3):
                query = "INSERT INTO `vcf_analyzer`.`PL` VALUES ('{}', '{}', '{}', '{}', NULL)".format(indId, values[0], values[1], values[2])
            elif (len(values) == 4):
                query = "INSERT INTO `vcf_analyzer`.`PL` VALUES ('{}', '{}', '{}', '{}', '{}')".format(indId, values[0], values[1], values[2], values[3])
            
            try:
                self.cursor.execute(query)
                self.cnx.commit()
            except:
                self.cnx.rollback()
                return -1
        
    def createGQ(self, indID, gqStr):
        query = "INSERT INTO `vcf_analyzer`.`GQ` VALUES ('{}', '{}')".format(indID, gqStr)
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        except:
            self.cnx.rollback()
            return -1
    
    def createHQ(self, indID, hqStr):
        
        if (hqStr == "."):
            return #not sure what to do about this case
        else:
            haplos = hqStr.split(',')
            query = "INSERT INTO `vcf_analyzer`.`HQ` VALUES ('{}', '{}', '{}')".format(indID, haplos[0], haplos[1])

            try:
                self.cursor.execute(query)
                self.cnx.commit()
            except:
                self.cnx.rollback()
                return -1
        
    def createPS(self, indID, psStr):
        query = "INSERT INTO `vcf_analyzer`.`PS` VALUES ('{}', '{}')".format(indID, psStr)
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        except:
            self.cnx.rollback()
            return -1
            
    def createPQ(self, indID, pqStr):
        query = "INSERT INTO `vcf_analyzer`.`PQ` VALUES ('{}', '{}')".format(indID, pqStr)
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
        except:
            self.cnx.rollback()
            return -1
            
    def createGLE(self, indId, gleStr):
        #NOTE no good example found; assuming comma separated
        values = gleStr.split(",")
        for i in range (len(values)):
            if (values[i] == "."):
                values[i] = "NULL"
            else:
                values[i] = "'" + values[i] + "'"
        for i in range (len(values), 9):
            values.append("NULL")
            
        query = "INSERT INTO `vcf_analyzer`.`GLE` VALUES ('{}','{}','{}','{}','{}','{}','{}','{}','{}','{}')".format(
                    indId, values[0], values[1], values[2], values[3],
                    values[4], values[5], values[6], values[7], values[8] )
        try:
            self.cursor.execute(query)
            self.cnx.commit()
            return 0
        except:
            self.cnx.rollback()
            return -1
        
    def createEC(self, indId, ecStr):
        values = ecStr.split(",")
        for i in range (len(values)):
            if (values[i] == "."):
                values[i] = "NULL"
            else:
                values[i] = "'" + values[i] + "'"
        for i in range (len(values), 4):
            values.append("NULL")
            
        query = "INSERT INTO `vcf_analyzer`.`EC` VALUES ({}, {}, {}, {}, {} )".format(
                    indId, values[0], values[1], values[2], values[3])
        try:
            self.cursor.execute(query)
            self.cnx.commit()
            return 0
        except:
            self.cnx.rollback()
            return -1
        
    def createAD(self, indId, adStr):
    
        values = adStr.split(",")
        for i in range (len(values)):
            if (values[i] == "."):
                values[i] = "NULL"
            else:
                values[i] = "'" + values[i] + "'"
        for i in range (len(values), 3):
            values.append("NULL")
            
        query = "INSERT INTO `vcf_analyzer`.`AD` VALUES ({}, {}, {}, {} )".format(
                    indId, values[0], values[1], values[2])
        
        try:
            self.cursor.execute(query)
            self.cnx.commit()
            return 0
        except:
            self.cnx.rollback()
            return -1
            
    def createGP(self, indId, gpStr):
        values = gpStr.split(",")
        for i in range (len(values)):
            if (values[i] == "."):
                values[i] = "NULL"
            else:
                values[i] = "'" + values[i] + "'"
        for i in range (len(values), 6):
            values.append("NULL")
            
        query = "INSERT INTO `vcf_analyzer`.`GP` VALUES ({}, {}, {}, {}, {}, {}, {} )".format(
                    indId, values[0], values[1], values[2], values[3], values[4], values[5])
        try:
            self.cursor.execute(query)
            self.cnx.commit()
            return 0
        except:
            self.cnx.rollback()
            return -1
            
    def createIndividualDefault(self, tableName, indId, valStr):
    
        try:
            query = "SELECT 'Count' FROM GenotypeTable WHERE GenoName ='" + tableName +"'"
            self.cursor.execute(query)
            retrieved = self.cursor.fetchone()
            if ( retrieved is None ):
                #table DNE
                return -1
            valCount = retrieved[0]
            query = "INSERT INTO `vcf_analyzer`.`{}` VALUES ('{}'".format(tableName, indId)
            
            if (valCount ==0 ):
                query.append( ")" )
            if (valCount == 1):
                query.append( ", '" + valStr + "')" )
            else:
                values = valStr.split(",")
                for val in values:
                    if (val == "."):
                        query.append( ", NULL" )
                    else:
                        query.append( ", '" + val + "'" )
                query.append( ")" )
                
            self.cursor.execute(query)
            
        except:
            self.cnx.rollback()
            return -1
        return 0

    def createVcfHeader(self, vcfId, header):
        
        query = "INSERT INTO `vcf_analyzer`.`VcfHeader` VALUES ({}, {} )".format(
                    vcfId, header)
        try:
            self.cursor.execute(query)
            self.cnx.commit()
            return 0
        except:
            self.cnx.rollback()
            return -1
    
    
    
#TODO individual
#JULIA:
#ADAM: GT, FT, PL, GQ, HQ, PS, PQ
#http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
        
        
        

def stringToTypeEnum( string ):
    if ( string == "Integer" ):
        dataType = "INT"
        typeEnum = 0
    elif ( string == "Float" ):
        dataType = "FLOAT"
        typeEnum = 1
    elif ( string == "String" ):
        dataType = "VARCHAR(75)"
        typeEnum = 5
    elif ( string == "Flag" ):
        dataType = "TINYINT UNSIGNED"
        typeEnum = 2
    return typeEnum, dataType
        
def __update_readme():
    import sys, vcf
    file('README.rst', 'w').write(vcf.__doc__)


# backwards compatibility
VCFReader = Reader
VCFWriter = Writer
