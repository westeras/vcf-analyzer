import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author chappljd.
 *         Created Mar 23, 2013.
 */
public class TestFilter {

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";
	
	static final String USER = "vcf_user";
	static final String PASS = "vcf";
	
	public static void main(String[] args) throws Exception {
		FilterApplier command = new FilterWriteApplier("2013-04-10_13:54", "", "FilterTest.txt" );
		System.out.println( command.execute() );
		fileCompare("FilterTest.txt", "FilterPass.txt");
		
		Connection conn = null;
		Statement stmt = null;		
		try {
			
			Class.forName(JDBC_DRIVER);
	
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
		}
		catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		
		String sql = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier'";
		String sql2 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier2'";
		
		try {
			stmt.executeUpdate(sql);
			stmt.executeUpdate(sql2);

		} catch (SQLException se) {
			throw new SQLException(se.getMessage());
		}
		
		String filterName = "testFilterApplier";
		String[] operands = {"entry AC=7"};
		FilterCreator testCreator = new FilterCreator(filterName, operands);
		testCreator.uploadEntries();
		
		//second Test
		command = new FilterWriteApplier("2013-04-10_13:54", "testFilterApplier", "FilterTest2.txt" );
		System.out.println( command.execute() );
		fileCompare("FilterTest2.txt", "FilterPass2.txt");
		
		filterName = "testFilterApplier2";
		String[] operands2 = {"entry REF=T"};//, "entry MQ between 78.8 79.0"};
		testCreator = new FilterCreator(filterName, operands2);
		testCreator.uploadEntries();
		
		command = new FilterWriteApplier("testFilter", "testFilterApplier2", "FilterTest3.txt" );
		System.out.println( command.execute() );
		
	}

	private static void fileCompare(String fileTestName, String filePassName) throws FileNotFoundException, IOException {
		//compare files
		BufferedReader testBuffer = new BufferedReader(new FileReader(fileTestName));
		BufferedReader goalBuffer = new BufferedReader(new FileReader(filePassName));
		
		String goal = goalBuffer.readLine();
		String toTest = testBuffer.readLine();
		while ( goal != null && toTest != null )
		{
			if ( !goal.equals(toTest) )
			{
				break;
			}
			goal = goalBuffer.readLine();
			toTest = testBuffer.readLine();
		}
		
		if ( goal == null && toTest == null )
		{
			System.out.println("PASS: Filter test");
		}
		else
		{
			System.out.println(String.format( "FAIL: Filter test\n\tgoal: %s\n\tfile: %s", goal, toTest) );
		}
		
		testBuffer.close();
		goalBuffer.close();
	}

}
