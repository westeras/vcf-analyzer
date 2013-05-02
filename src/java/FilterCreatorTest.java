import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class FilterCreatorTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
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
		}
		catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		
		String sql = "DELETE FROM `vcf_analyzer`.`Filter` WHERE `FilName`='testFilter'";
		
		try {
			stmt.executeUpdate(sql);

		} catch (SQLException se) {
			throw new SQLException(se.getMessage());
		}
		
		String filterName = "testFilter";
		String[] operands = {"entry AC = 4", "entry DS between 2 10", "entry bogus < 55", "ent AC not exists", "ent AC != 5",
									"ind AD>1", "ind DP=0", "ind PQ between 10 15", "ind AD != 3", "ind PQ exists",
									"option FailureAllow=5", "option pe equals 34"};
		FilterCreator testCreator = new FilterCreator(filterName, operands);
		testCreator.uploadEntries();
	}
}
