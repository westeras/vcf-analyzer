import vcf
vcf_reader = vcf.Reader(open('../testvcf2.vcf'))

fileEnded = False
while (not fileEnded):
    fileEnded = vcf_reader.next()


