import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * TODO Put here a description of what this class does.
 *
 * @author chappljd.
 *         Created May 3, 2013.
 */
public class TestLogin {

	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception{

		Connection conn;
		Statement stmt;

		if (DatabaseLogin.DB_URL == null)
		{
			DatabaseLogin.uploadLogin();
		}
		
		Class.forName(DatabaseLogin.JDBC_DRIVER);

		conn = DriverManager.getConnection(DatabaseLogin.DB_URL,
				DatabaseLogin.USER, DatabaseLogin.PASS);
		stmt = conn.createStatement();
		
		stmt.close();
		conn.close();

	}

}
