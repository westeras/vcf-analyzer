import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
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
	 * Takes the file to be parsed
	 *
	 * @param divFile
	 */
	public DivergenceParser(File divFile){
		this.divergenceFile=divFile;
	}
	


	public ArrayList<String[]> parseFile() throws FileNotFoundException, ClassNotFoundException, SQLException{
		FileInputStream fileIn=new FileInputStream(this.divergenceFile);
		Scanner reader=new Scanner(fileIn);
		DatabaseConnector connection=new DatabaseConnector();
		ArrayList<String[]> rows = new ArrayList<String[]>();
		while (reader.hasNextLine()){
			String[] columns=parseLine(reader.nextLine());
			rows.add(columns);
		}
		return rows;
	}
	private String[] parseLine(String line){
		StringTokenizer tokenizer=new StringTokenizer(line);
		String chrom =tokenizer.nextToken();
		String position=tokenizer.nextToken();
		String divValue=tokenizer.nextToken();
		return new String[] {chrom, position, divValue};
	}
}
