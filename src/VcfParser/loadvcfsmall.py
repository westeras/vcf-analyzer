import vcf
vcf_reader = vcf.Reader(open('/home/vcf_repository/vcf-analyzer/src/smallestvcf.vcf'))

fileEnded = False
while (not fileEnded):
    fileEnded = vcf_reader.next()


