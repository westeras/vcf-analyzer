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
	
	public static void main(String[] args) throws Exception {
		
		//TODO imprive tests
		
		/*
		FilterApplier command = new FilterWriteApplier("filterVcf", "", "FilterTest.txt" );
		System.out.println( command.execute() );
		fileCompare("FilterTest.txt", "FilterPass.txt");
		*/
		
		Connection conn = null;
		Statement stmt = null;		
		try {
			if (DatabaseLogin.DB_URL == null)
			{
				DatabaseLogin.uploadLogin();
			}
			Class.forName(DatabaseLogin.JDBC_DRIVER);
	
			conn = DriverManager.getConnection(DatabaseLogin.DB_URL, DatabaseLogin.USER, DatabaseLogin.PASS);
			stmt = conn.createStatement();
		
			String sql = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier'";
			String sql2 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier2'";
			String sql3 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier3'";
			String sql4 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier4'";
			String sql5 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier5'";
			String sql6 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier6'";
			String sql7 = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilterApplier7'";
			String sql8 = "DELETE FROM `vcf_analyzer`.`FilterEntryPass` WHERE 1";
			String sql9 = "DELETE FROM `vcf_analyzer`.`FilterIndividualPass` WHERE 1";

			stmt.executeUpdate(sql);
			stmt.executeUpdate(sql2);
			stmt.executeUpdate(sql3);
			stmt.executeUpdate(sql4);
			stmt.executeUpdate(sql5);
			stmt.executeUpdate(sql6);
			stmt.executeUpdate(sql7);
			stmt.executeUpdate(sql8);
			stmt.executeUpdate(sql9);
		
			/*
			String filterName = "testFilterApplier";
			String[] operands = {"entry AC=7"};
			FilterCreator testCreator = new FilterCreator(filterName, operands);
			testCreator.uploadEntries();
			
			//second Test
			command = new FilterWriteApplier("filterVcf", "testFilterApplier", "FilterTest2.txt" );
			System.out.println( command.execute() );
			fileCompare("FilterTest2.txt", "FilterPass2.txt");
			
			//store test
			command = new FilterStoreApplier("filterVcf", "" );
			System.out.println( command.execute() );
			command = new FilterStoreApplier("filterVcf", filterName );
			System.out.println( command.execute() );
			*/
			
			//Test 3 removed
			
			//Test 4 removed	
			
			String filterName;
			FilterApplier command;
			FilterCreator testCreator;
			
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
			fileCompare("FilterTest6.txt", "FilterPass6.txt");
			
			filterName = "testFilterApplier6";
			String[] operands6 = {"entry REF=T", "ind DP<41", "option pe equals 3"};
			testCreator = new FilterCreator(filterName, operands6);
			testCreator.uploadEntries();
			command = new FilterWriteApplier("testFilter", "testFilterApplier6", "FilterTest7.txt" );
			System.out.println( command.execute() );
			fileCompare("FilterTest7.txt", "FilterPass7.txt");
			
			filterName = "testFilterApplier7";
			String[] operands7 = {"ind GT=0/0", "option FailureAllow=10"};
			testCreator = new FilterCreator(filterName, operands7);
			testCreator.uploadEntries();
			command = new FilterWriteApplier("testFilter", "testFilterApplier7", "FilterTest8.txt" );
			System.out.println( command.execute() );
			fileCompare("FilterTest8.txt", "FilterPass8.txt");
			
		
		} catch (SQLException se) {
			throw new SQLException(se.getMessage());
		}
		
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
