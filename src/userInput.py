import vcf

def uploadVcfWithoutName(location):
    print(location)

def uploadVcfWithName(location, name):
    print(location)
    print(name)

breaker = 0
while(breaker == 0):
    print('Please input a command')
    command = input('>').split()
    print('You input ' + command[0])
    if command[0] == "quit":
        breaker = 1
    elif command[0] == "upload" and command[1] == "vcf":
        if len(command) == 3:
            uploadVcfWithoutName(command[2])
        elif len(command) == 4:
            uploadVcfWithName(command[2], command[3])
    elif command[0] == "upvcf" or command[0] == "uploadvcf":
        if len(command) == 2:
            uploadVcfWithoutName(command[1])
        elif len(command) == 3:
            uploadVcfWithName(command[1], command[2])
    else:
        print("Run Java here")
