import argparse
import subprocess
import sys
sys.path.append('/VcfParser/vcf')
import vcf

def uploadVcfWithoutName(location):
    vcf_reader = vcf.Reader(open(location))

    fileEnded = False
    while (not fileEnded):
        fileEnded = vcf_reader.next()

    return "Upload successful"

def uploadVcfWithName(location, name):
    vcf_reader = vcf.Reader(vcfName=name, fsock=open(location))

    fileEnded = False
    while (not fileEnded):
        fileEnded = vcf_reader.next()

    return "Upload successful"

parser = argparse.ArgumentParser(description='Reads input for the VCF Handler')
parser.add_argument("-cp", "--classpath")
parser.add_argument("-com","--command")

args = parser.parse_args()

if args.classpath is None:
    cp = "./java:$CLASSPATH"
else:
    cp = args.classpath

if args.command is None:
    breaker = 0
    print('Please input a command')
    while(breaker == 0):
        name = ""
        fileName = ""
        inputString = raw_input('>')
        if inputString == "" or inputString == "help":
            print('Please see the User Guide')
        else:
            command = inputString.split()
            if command[0] == "quit":
                breaker = 1
            elif command[0] == "upload" and command[1] == "vcf" or command[0] == "upvcf" or command[0] == "uploadvcf":
                for pos in range(len(command)):
                    if command[pos] == "name" and pos != len(command) - 1:
                        name = command[pos + 1]
                    if command[pos] == "file" and pos != len(command) - 1:
                        fileName = command[pos + 1]
                if fileName != "" and name != "":
                    print(uploadVcfWithName(fileName, name))
                if fileName != "" and name == "":
                    print(uploadVcfWithoutName(fileName))
                if fileName == "":
                    print('Please include additional arguments')
                    
            else:
                command = "java -cp " + cp  + " CommandLineInterpreter " + inputString
                #print command
                subprocess.call(command, shell=True)
else:
    command = "java -cp " + cp  + " " + args.command
    print command
    subprocess.call(command, shell=True)