import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class ParserTest {

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";
	
	static final String USER = "vcf_user";
	static final String PASS = "vcf";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
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
		
		String sql = "DELETE FROM `vcf_analyzer`.`Vcf` WHERE `VcfName`='testVCF_adam'";
		
		try {
			stmt.executeUpdate(sql);

		} catch (SQLException se) {
			throw new SQLException(se.getMessage());
		}
		
		String vcfName = "testFilter";
	}

}
