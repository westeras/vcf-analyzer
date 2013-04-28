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
		String sql3 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier3'";
		String sql4 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier4'";
		String sql5 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier5'";
		String sql6 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier6'";
		String sql7 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier7'";
		String sql8 = "DELETE FROM `vcf_analyzer`.`FilterEntryPass` WHERE 1";
		String sql9 = "DELETE FROM `vcf_analyzer`.`FilterIndividualPass` WHERE 1";
		
		try {
			stmt.executeUpdate(sql);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql3);
			stmt.executeUpdate(sql4);
			stmt.executeUpdate(sql5);
			stmt.executeUpdate(sql6);
			stmt.executeUpdate(sql7);
			stmt.executeUpdate(sql8);
			stmt.executeUpdate(sql9);

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
		
		//store test
		command = new FilterStoreApplier("2013-04-10_13:54", "" );
		System.out.println( command.execute() );
		command = new FilterStoreApplier("2013-04-10_13:54", filterName );
		System.out.println( command.execute() );
		
		filterName = "testFilterApplier2";
		String[] operands2 = {"entry REF=T"};//, "entry MQ between 78.8 79.0"};
		testCreator = new FilterCreator(filterName, operands2);
		testCreator.uploadEntries();
		
		command = new FilterWriteApplier("testFilter", "testFilterApplier2", "FilterTest3.txt" );
		System.out.println( command.execute() );
		fileCompare("FilterTest3.txt", "FilterPass3.txt");
		
		filterName = "testFilterApplier3";
		String[] operands3 = {"entry REF=T", "entry MQ not equals 0", "entry Dels=0"};//"entry MQ between 78.8 79.0"};
		testCreator = new FilterCreator(filterName, operands3);
		testCreator.uploadEntries();
		
		command = new FilterWriteApplier("testFilter", "testFilterApplier3", "FilterTest4.txt" );
		System.out.println( command.execute() );
		//fileCompare("FilterTest4.txt", "FilterPass4.txt");	
		
		filterName = "testFilterApplier4";
		String[] operands4 = {"entry Dels=0", "entry MQ between 78.8 79"};
		testCreator = new FilterCreator(filterName, operands4);
		testCreator.uploadEntries();
		
		command = new FilterWriteApplier("testFilter", "testFilterApplier4", "FilterTest5.txt" );
		System.out.println( command.execute() );
		
		filterName = "testFilterApplier5";
		String[] operands5 = {"entry Dels=0","entry BaseQRankSum exists"};
		testCreator = new FilterCreator(filterName, operands5);
		testCreator.uploadEntries();
		
		command = new FilterWriteApplier("testFilter", "testFilterApplier5", "FilterTest6.txt" );
		System.out.println( command.execute() );
		
		filterName = "testFilterApplier6";
		String[] operands6 = {"entry REF=T", "ind DP<41", "option pe equals 3"};
		testCreator = new FilterCreator(filterName, operands6);
		testCreator.uploadEntries();
		command = new FilterWriteApplier("testFilter", "testFilterApplier6", "FilterTest7.txt" );
		System.out.println( command.execute() );
		
		
		filterName = "testFilterApplier7";
		String[] operands7 = {"ind GT=0/0", "option FailureAllow=10"};
		testCreator = new FilterCreator(filterName, operands7);
		testCreator.uploadEntries();
		command = new FilterWriteApplier("testFilter", "testFilterApplier7", "FilterTest8.txt" );
		System.out.println( command.execute() );
		fileCompare("FilterTest8.txt", "FilterPass8.txt");
		
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
			System.out.println(String.format( "FAIL: Filter test\n\tgoal: %s\n\tfile: %s", fileTestName, filePassName) );
		}
		
		testBuffer.close();
		goalBuffer.close();
	}

}
