import argparse
import subprocess
import vcf

def uploadVcfWithoutName(location):
    vcf_reader = vcf.Reader(open(location))

    fileEnded = False
    while (not fileEnded):
        fileEnded = vcf_reader.next()

def uploadVcfWithName(location, name):
    vcf_reader = vcf.Reader(vcfName=name, fsock=open(location))

    fileEnded = False
    while (not fileEnded):
        fileEnded = vcf_reader.next()

parser = argparse.ArgumentParser(description='Reads input for the VCF Handler')
parser.add_argument("-cp", "--classpath")
parser.add_argument("-com","--command")

args = parser.parse_args()

if args.classpath is None:
    cp = "../java:$CLASSPATH"
else:
    cp = args.classpath

if args.command is None:
    breaker = 0
    while(breaker == 0):
        print('Please input a command')
        inputString = raw_input('>')
        if inputString == "" or inputString == "help":
            print('Get help') #Call to java properly later
        else:
            command = inputString.split()
            if command[0] == "quit":
                breaker = 1
            elif command[0] == "upload" and command[1] == "vcf":
                if len(command) == 2:
                    print('Please input file location')
                if len(command) == 3:
                    uploadVcfWithoutName(command[2])
                if len(command) == 4:
                    uploadVcfWithName(command[2], command[3])
            elif command[0] == "upvcf" or command[0] == "uploadvcf":
                if len(command) == 1:
                    print('Please input file location')
                if len(command) == 2:
                    uploadVcfWithoutName(command[1])
                if len(command) == 3:
                    uploadVcfWithName(command[1], command[2])
            else:
                command = "java -cp " + cp  + " CommandLineInterpreter -" + inputString
                print command
                subprocess.call(command, shell=True)
else:
    command = "java -cp " + cp  + " " + args.command
    print command
    subprocess.call(command, shell=True)
