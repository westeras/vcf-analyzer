import java.io.FileNotFoundException;
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
		Command uploadDivCommand= new UploadDivergenceCommand("C:\\Users\\schepedw\\Documents\\Courses\\CSSE 220\\JavaProjects\\vcf-tester\\Examples\\divergence.txt","");
		uploadDivCommand.execute();
		
	}

}
