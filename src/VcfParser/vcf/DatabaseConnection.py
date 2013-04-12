import mysql.connector
import datetime

class DatabaseConnection():

    def __init__(self):
        #Connect to database
        ########### Why are there hard coded username and password information
        self.cnx = mysql.connector.connect(user = 'vcf_user', password = 'vcf', host = 'localhost', database = 'vcf_analyzer', buffered=True)
        self.cursor = self.cnx.cursor()
        
    def dbClose(self):
        self.cursor.close()
        self.cnx.close()
        
    def commitQuery(self, query):
        try:
            self.execAndCommit(query)
        except:
            self.exceptionHandle(query)
            
    def execAndCommit(self, query):
        self.cursor.execute(query)
        self.cnx.commit()

    def exceptionHandle(self, query):
        self.cnx.rollback()
        print query
        return -1
    
    ########### Kind of a long method
    def handleInfo(self, infoName, infoData):
        query = ("SELECT count(*) FROM InfoTable WHERE InfoName='" + infoName + "'")
        self.cursor.execute(query)
        count = self.cursor.fetchone()[0]
        if (count == 0):
            dataType = None
            typeEnum = 0;
            typeEnum, dataType = stringToTypeEnum( infoData.type )    
            
            rowCount = 1
            print ">>>>>",
            print infoData
            if (infoData.type == "Flag"):
                rowCount = 0;
            elif ( infoData.num != None ):
                print ">>>>>",
                print infoData.num
                rowCount = int(infoData.num)
                
            createQuery = ("CREATE TABLE `{}` ( EntryId BIGINT NOT NULL PRIMARY KEY").format( infoName )
            for i in range(rowCount):
                createQuery += (", `val{}` {} DEFAULT NULL").format( i, dataType )
            createQuery += ")"
            
            constraint = ("ALTER TABLE  `{}` ADD FOREIGN KEY ( `EntryId` ) ").format(infoName)
            constraint += " REFERENCES `vcf_analyzer`.`VcfEntry` (`EntryId`) ON DELETE CASCADE ON UPDATE CASCADE"
            
            infoQuery = ("INSERT INTO `vcf_analyzer`.`InfoTable` (`InfoName`, `Type`, `Count`, `Description`) " +
                        "VALUES ( '{}', '{}', '{}', '{}' )").format(
                        infoName, typeEnum, rowCount, infoData.desc[:150] )
            try:
                self.cursor.execute(createQuery)
                self.cursor.execute(constraint)
                self.cursor.execute(infoQuery)
                self.cnx.commit()
                
            except:
                self.exceptionHandle(query)

    def insertInfo(self, entryDbId, entry):
        
        if (len(entry) == 2):
            query = "INSERT INTO `vcf_analyzer`.`{}` VALUES ('{}', '{}')".format(entry[0], entryDbId, entry[1])
        else:
            query = "INSERT INTO `vcf_analyzer`.`{}` VALUES ('{}')".format(entry[0], entryDbId)
        
        self.commitQuery(query)

    def handleFormat(self, formatName, formatData):
    
        query = ("SELECT count(*) FROM GenotypeTable WHERE GenoName='" + formatName + "'")
        self.cursor.execute(query)
        count = self.cursor.fetchone()[0]
        if (count == 0):
        
            if ( len(formatName) > 5 ):
                #Name too long
                return -1
                
            dataType = None
            typeEnum = 0;
            rowCount, dataType = stringToTypeEnum( formatData.type )
            
            rowCount = 1
            if ( formatData.num == None and formatData.type == "Flag"):
                rowCount = 0;
                typeEnum = 2
            elif ( formatData.num != None ):
                rowCount = int(formatData.num)
                
            createQuery = ("CREATE TABLE `{}` ( IndID BIGINT NOT NULL PRIMARY KEY").format( formatName )
            for i in range(rowCount):
                createQuery += (", `val{}` {}").format( i, dataType )
            createQuery += ")"
            
            constraint = ("ALTER TABLE  `{}` ADD FOREIGN KEY ( `IndID` ) ").format(formatName)
            constraint += " REFERENCES `vcf_analyzer`.`IndividualEntry` (`IndID`) ON DELETE CASCADE ON UPDATE CASCADE"
            
            infoQuery = ("INSERT INTO `vcf_analyzer`.`GenotypeTable` (`GenoName`, `Type`, `Count`, `Description`) " +
                        "VALUES ( '{}', '{}', '{}', '{}' )").format(
                        formatName, typeEnum, rowCount, formatData.desc[:150] )
            try:
                self.cursor.execute(createQuery)
                self.cursor.execute(constraint)
                self.cursor.execute(infoQuery)
                self.cnx.commit()
                
            except:
                self.exceptionHandle(query)
                
    
    ########### Many many parameters being passed in 
    def createEntry(self, vcfId, chrom, pos, id, ref, alt, qual, filter, format):
        query = ("INSERT INTO `vcf_analyzer`.`VcfEntry` (`EntryId`, `VcfId`, `Chrom`, `Pos`, `Id`, `Ref`, `Alt`, `Qual`, `Filter`, `Format`) " +
                "VALUES ( NULL, '{}', '{}', '{}', '{}', '{}', '{}', '{}', '{}', '{}')").format(
                vcfId, chrom, pos, id, ref, alt, qual, filter, format )
        
        try:
            self.execAndCommit(query)
        
            query = ("SELECT max(EntryId) FROM VcfEntry ")
            self.cursor.execute(query)
            return self.cursor.fetchone()[0]
        except:
            self.exceptionHandle(query)

    def createVcfIndividuals(self, vcfId, names ):
    
        queries = []
        indNo = 0
        for name in names:
            queries.append( ("INSERT INTO `vcf_analyzer`.`VcfIndividual` ( `VcfId`, `IndNoVcf`, `IndName`) " +
                            "VALUES ( '{}', '{}', '{}' )").format(
                            vcfId, indNo, name[:25] ) )
            indNo += 1
        
        try:
            for q in queries:
                self.cursor.execute(q)
            self.cnx.commit()
            return indNo
        except:
            self.exceptionHandle(query)
            
    def createIndividualEntry(self, entryId, indNo ):
        query = ("INSERT INTO `vcf_analyzer`.`IndividualEntry` ( `IndID`, `EntryId`, `IndNoVcf`) " +
                "VALUES ( NULL, '{}', '{}' )").format(
                entryId, indNo )
        
        try:
            self.execAndCommit(query)
        
            query2 = ("SELECT max(IndID) FROM IndividualEntry ")
            self.cursor.execute(query2)
            return self.cursor.fetchone()[0]
        except:
            self.exceptionHandle(query)
            
    def createVcf( self, name ):
        
        query = ("SELECT VcfName FROM Vcf ORDER BY VcfId DESC")
        first = ""
        
        try:
            self.cursor.execute(query)
            first = self.cursor.fetchone()
        except:
            self.exceptionHandle(query)   
            
        lastName = None
        if ( first!= None and len(first) > 0 ):
            lastName = first[0]
        if ( lastName != None ):
            if ( name in lastName):
                if ( name == lastName ):
                    name = name + "-1"
                else:
                    name = lastName[:-1] + str( int(lastName[-1]) + 1 )

        query = ("INSERT INTO `vcf_analyzer`.`Vcf` (`VcfId`, `VcfName`) " +
                "VALUES ( NULL, '{}')").format(
                name[:75] )

        try:
            self.execAndCommit(query)
        
            query = ("SELECT max(VcfId) FROM Vcf")
            self.cursor.execute(query)
            return self.cursor.fetchone()[0]
        except:
            self.exceptionHandle(query)
            
    def createDP( self, indId, dpStr ):
    
        if (dpStr == "."):
            return;
            
        query = ("INSERT INTO `vcf_analyzer`.`DP` (`IndID`, `ReadDepth`) " +
                "VALUES ( '{}', '{}' )").format(
                indId, dpStr )
        
        self.commitQuery(query)
            
    def createGL( self, indId, glStr ):
    
        query = ""
        if ( glStr == "." ):
            return
        else:
            entries = glStr.split(',')
            entries = self.editValuesForDB(entries)

            if ( len(entries) == 3 ):
                query = ("INSERT INTO `vcf_analyzer`.`GL` (`IndID`, `AA`, `AB`, `BB`) " +
                        "VALUES ( {}, {}, {}, {} )").format(
                        indId, entries[0], entries[1], entries[2] )
            elif (len(entries) == 6 ):
                query = ("INSERT INTO `vcf_analyzer`.`GL` (`IndID`, `AA`, `AB`, `BB`, `AC`, `BC`, `CC`) " +
                        "VALUES ( {}, {}, {}, {}, {}, {}, {} )").format(
                        indId, entries[0], entries[1], entries[2], entries[3], entries[4], entries[5] )
            else:
                #invalid number of GL values
                return -1
            
        self.commitQuery(query)

    def createGT(self, indID, gtStr):
    
        if (gtStr == "."):
            return #not sure what to do about this case
        gtStrList = list(gtStr)
        outList = []
        n = len(gtStr)

        for i in range(n):
            if (gtStrList[i] == '/'):
                outList.append("'0'")
            elif (gtStrList[i] == '|'):
                outList.append("'1'")
            elif (gtStrList[i] == '.'):
                outList.append("NULL")
            else:
                outList.append( "'" + gtStrList[i] + "'")
        
        ########### This is a place where doing the static query builder along with Python arrays could reduce this
        if (n == 1):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ({}, {}, NULL, NULL, NULL, NULL)".format(indID, outList[0])
        elif (n == 2):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ({}, {}, {}, NULL, NULL, NULL)".format(indID, outList[0], outList[1])
        elif (n == 3):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ({}, {}, {}, {}, NULL, NULL)".format(indID, outList[0], outList[1], outList[2])
        elif (n == 4):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ({}, {}, {}, {}, {}, NULL)".format(indID, outList[0], outList[1], outList[2], outList[3])
        elif (n == 5):
            query = "INSERT INTO `vcf_analyzer`.`GT` VALUES ({}, {}, {}, {}, {}, {})".format(indID, outList[0], outList[1], outList[2], outList[3], outList[4])
        
        self.commitQuery(query)
        
        return
        
    def createFT(self, indID, ftStr):
        if (ftStr == "."):
            return #not sure what to do about this case
    
        query = "INSERT INTO `vcf_analyzer`.`FT` VALUES ('{}', '{}')".format(indID, ftStr)
        
        self.commitQuery(query)
            
    def createPL(self, indId, plStr):
        if (plStr == "."):
            return #not sure what to do about this case
        else:
            values = plStr.split(',')
            
            if (len(values) == 1):
                query = "INSERT INTO `vcf_analyzer`.`PL` VALUES ('{}', '{}', NULL, NULL, NULL)".format(indId, values[0])
            elif (len(values) == 2):
                query = "INSERT INTO `vcf_analyzer`.`PL` VALUES ('{}', '{}', '{}', NULL, NULL)".format(indId, values[0], values[1])
            elif (len(values) == 3):
                query = "INSERT INTO `vcf_analyzer`.`PL` VALUES ('{}', '{}', '{}', '{}', NULL)".format(indId, values[0], values[1], values[2])
            elif (len(values) == 4):
                query = "INSERT INTO `vcf_analyzer`.`PL` VALUES ('{}', '{}', '{}', '{}', '{}')".format(indId, values[0], values[1], values[2], values[3])
            
            self.commitQuery(query)
        
    def createGQ(self, indID, gqStr):
        if (gqStr == "."):
            return #not sure what to do about this case
    
        query = "INSERT INTO `vcf_analyzer`.`GQ` VALUES ('{}', '{}')".format(indID, gqStr)
        
        self.commitQuery(query)
    
    def createHQ(self, indID, hqStr):
        
        if (hqStr == "."):
            return #not sure what to do about this case
        else:
            haplos = hqStr.split(',')
            query = "INSERT INTO `vcf_analyzer`.`HQ` VALUES ('{}', '{}', '{}')".format(indID, haplos[0], haplos[1])

            self.commitQuery(query)
        
    def createPS(self, indID, psStr):
    
        if (psStr == "."):
            return #not sure what to do about this case
        query = "INSERT INTO `vcf_analyzer`.`PS` VALUES ('{}', '{}')".format(indID, psStr)
        
        self.commitQuery(query)
            
    def createPQ(self, indID, pqStr):
    
        if (pqStr == "."):
            return #not sure what to do about this case
        query = "INSERT INTO `vcf_analyzer`.`PQ` VALUES ('{}', '{}')".format(indID, pqStr)
        
        self.commitQuery(query)
            
    def createGLE(self, indId, gleStr):
        #NOTE no good example found; assuming comma separated
        if (gleStr == "."):
            return #not sure what to do about this case
        
        values = gleStr.split(",")
        values = self.editValuesForDB(values)
        for i in range (len(values), 9):
            values.append("NULL")
            
        query = "INSERT INTO `vcf_analyzer`.`GLE` VALUES ({},{},{},{},{},{},{},{},{},{})".format(
                    indId, values[0], values[1], values[2], values[3],
                    values[4], values[5], values[6], values[7], values[8] )

        self.commitQuery(query)
        
    def createEC(self, indId, ecStr):
    
        if (ecStr == "."):
            return #not sure what to do about this case
        values = ecStr.split(",")
        values = self.editValuesForDB(values)
        for i in range (len(values), 4):
            values.append("NULL")
            
        query = "INSERT INTO `vcf_analyzer`.`EC` VALUES ({}, {}, {}, {}, {} )".format(
                    indId, values[0], values[1], values[2], values[3])
        
        self.commitQuery(query)
        
    def editValuesForDB(self, values):
        returnValues = values
        for i in range (len(returnValues)):
            if (returnValues[i] == "."):
                returnValues[i] = "NULL"
            else:
                returnValues[i] = "'" + returnValues[i] + "'"

        return returnValues

    def createAD(self, indId, adStr):
        if (adStr == "."):
            return #not sure what to do about this case
    
        values = adStr.split(",")
        values = self.editValuesForDB(values)
        for i in range (len(values), 3):
            values.append("NULL")
            
        query = "INSERT INTO `vcf_analyzer`.`AD` VALUES ({}, {}, {}, {} )".format(
                    indId, values[0], values[1], values[2])
        
        self.commitQuery(query)
            
    def createGP(self, indId, gpStr):
    
        if (gpStr == "."):
            return #not sure what to do about this case
        values = gpStr.split(",")
        values = self.editValuesForDB(values)
        for i in range (len(values), 6):
            values.append("NULL")
            
        query = "INSERT INTO `vcf_analyzer`.`GP` VALUES ({}, {}, {}, {}, {}, {}, {} )".format(
                    indId, values[0], values[1], values[2], values[3], values[4], values[5])

        self.commitQuery(query)
            
    def createIndividualDefault(self, tableName, indId, valStr):
    
        if (valStr == "."):
            return #not sure what to do about this case
    
        try:
            query = "SELECT 'Count' FROM GenotypeTable WHERE GenoName ='" + tableName +"'"
            self.cursor.execute(query)
            retrieved = self.cursor.fetchone()
            if ( retrieved is None ):
                #table DNE
                return -1
            valCount = retrieved[0]
            query = "INSERT INTO `vcf_analyzer`.`{}` VALUES ('{}'".format(tableName, indId)
            
            if (valCount ==0 ):
                query.append( ")" )
            if (valCount == 1):
                query.append( ", '" + valStr + "')" )
            else:
                values = valStr.split(",")
                for val in values:
                    if (val == "."):
                        query.append( ", NULL" )
                    else:
                        query.append( ", '" + val + "'" )
                query.append( ")" )
                
            self.cursor.execute(query)
            
        except:
            self.exceptionHandle(query)
        return 0

    def createVcfHeader(self, vcfId, header):
        header = header.replace("'", "`")
        
        query = "INSERT INTO `vcf_analyzer`.`VcfHeader` VALUES ('{}', '{}')".format(vcfId, header)
        self.commitQuery(query)

    #See the following link for VCF information
    #http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
    
    def stringToTypeEnum( string ):
        if ( string == "Integer" ):
            dataType = "INT"
            typeEnum = 0
        elif ( string == "Float" ):
            dataType = "FLOAT"
            typeEnum = 1
        elif ( string == "String" ):
            dataType = "VARCHAR(75)"
            typeEnum = 5
        elif ( string == "Flag" ):
            dataType = "TINYINT UNSIGNED"
            typeEnum = 2
        return typeEnum, dataType
            
    def __update_readme():
        import sys, vcf
        file('README.rst', 'w').write(vcf.__doc__)