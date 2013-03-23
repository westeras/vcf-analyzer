import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Mar 20, 2013.
 */
public class UploadTester {

	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, SQLException {
		System.out.println(testFormat("DivName","Chromosome",1,2));
//		Command uploadDivCommand= new UploadDivergenceCommand("Examples/divergence.txt","","");
//		uploadDivCommand.execute();
//		Command uploadAnnotCommand=new UploadAnnotationCommand("Examples/annot.txt","","");
//		uploadAnnotCommand.execute();
	}
	
	
	protected static String testFormat(String name, String chromosome,
			int position, int divValue) throws ClassNotFoundException,
			SQLException {

		String sql = String
				.format("INSERT into `Divergence` (`DivName`, `Chromosome`, `Position`, `DivValue`) VALUES ('%s','%s','%d','%d');",
						name, chromosome, position, divValue);
		return sql;

	}

}
