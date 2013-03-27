import vcf
vcf_reader = vcf.Reader(vcfName='testVCF_adam', fsock=open('../testVcf2.vcf'))

fileEnded = False
while (not fileEnded):
    fileEnded = vcf_reader.next()
