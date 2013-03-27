#import pycallgraph
#pycallgraph.start_trace()

import vcf
vcf_reader = vcf.Reader(vcfName='testVCF_adam', fsock=open('../testvcf2.vcf'))

fileEnded = False
while (not fileEnded):
    fileEnded = vcf_reader.next()
    
#pycallgraph.make_dot_graph('test.png')
