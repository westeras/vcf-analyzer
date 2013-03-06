import vcf
vcf_reader = vcf.Reader(open('/home/vcf_repository/vcf-analyzer/src/testvcf.vcf'))
record = vcf_reader.next()