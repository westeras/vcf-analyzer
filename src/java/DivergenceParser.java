import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.StringTokenizer;


/**
 * Consider creating a super class for this and AnnotationParser
 *
 * @author schepedw.
 *         Created Mar 17, 2013.
 */
public class DivergenceParser {
	private File divergenceFile;
	
	/**
	 * Takes the name of a file to be parsed.
	 * Alternatively, could actually take the file object.
	 *
	 * @param pathname
	 */
	public DivergenceParser(String pathname){
		this.divergenceFile=new File(pathname);
	}
	
	public void parseFile() throws FileNotFoundException, ClassNotFoundException, SQLException{
		FileInputStream fileIn=new FileInputStream(this.divergenceFile);
		Scanner reader=new Scanner(fileIn);
		DatabaseConnector connection=new DatabaseConnector();
		while (reader.hasNextLine()){
			String[] parsedLine=parseLine(reader.nextLine());
			connection.uploadDivergenceLine(parsedLine[0],Integer.valueOf(parsedLine[1]),Integer.valueOf(parsedLine[2]));
		}
		
	}
	private String[] parseLine(String line){
		StringTokenizer tokenizer=new StringTokenizer(line);
		String chrom =tokenizer.nextToken();
		String position=tokenizer.nextToken();
		String divValue=tokenizer.nextToken();
		return new String[] {chrom, position, divValue};
	}
}
