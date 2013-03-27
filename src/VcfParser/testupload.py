import vcf
vcf_reader = vcf.Reader(vcfName='testVCF_adam', fsock=open('../testInfoTable.vcf'))

fileEnded = False
while (not fileEnded):
    fileEnded = vcf_reader.next()
