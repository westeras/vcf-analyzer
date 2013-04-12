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
public class AFSTester {

	/**
	 * Tests both parsers
	 *
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, SQLException {
		AFSCommand command= new AFSCommand("testName","");
		command.execute();
		

	}

}
