import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Mar 23, 2013.
 */
public class ParserTester {

	/**
	 * Tests both parsers
	 *
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, SQLException {
		AnnotationParser annotParser=new AnnotationParser(new File("Examples/annot.txt"));
		                                                           
		ArrayList<String[]> rowsToUpload=annotParser.parseFile();
		for (String[] row : rowsToUpload){
			String chrom =row[0];
			String startPosition=row[1];
			String endPosition=row[2];
			String geneName=row[3];
			String geneDirection=row[4];
			System.out.printf("%s %s %s %s %s\n", chrom, startPosition, endPosition, geneName, geneDirection);
		}	
		System.out.println("Done testing annotation parser");
		DivergenceParser divParser=new DivergenceParser(new File("Examples/divergence.txt"));
		rowsToUpload=divParser.parseFile();
		for (String[] row : rowsToUpload){
			String chromosome=row[0];
			String position=row[1];
			String divValue=row[2];
			System.out.printf("%s %s %s \n", chromosome, position, divValue);
		}	
		System.out.println("Done testing div parser");
		
		

	}

}
