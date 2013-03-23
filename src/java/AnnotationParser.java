import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Mar 17, 2013.
 */
public class AnnotationParser {
	
	

	private File annotationFile;
	public AnnotationParser(File annotFile){
		this.annotationFile=annotFile;
	}
	

	public ArrayList<String[]> parseFile() throws FileNotFoundException, ClassNotFoundException, SQLException{
		FileInputStream fileIn=new FileInputStream(this.annotationFile);
		Scanner reader=new Scanner(fileIn);
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
		String startPosition=tokenizer.nextToken();
		String endPosition=tokenizer.nextToken();
		String geneName=tokenizer.nextToken();
		String geneDirection=tokenizer.nextToken();
		return new String[] {chrom, startPosition, endPosition,geneName,geneDirection};
	}
}
