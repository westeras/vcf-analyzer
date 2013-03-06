import vcf
vcf_reader = vcf.Reader(open('/home/vcf_repository/vcf-analyzer/src/testvcf3.vcf'), vcfName = "testName")
record = vcf_reader.next()

record = vcf_reader.next()

record = vcf_reader.next()

