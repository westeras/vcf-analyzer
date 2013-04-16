import argparse
import subprocess

parser = argparse.ArgumentParser(description='Reads input for the VCF Handler')
parser.add_argument("-cp", "--classpath")
parser.add_argument("command")

args = parser.parse_args()
command = "java -cp " + args.classpath  + " " + args.command
print command
subprocess.call(command, shell=True)
