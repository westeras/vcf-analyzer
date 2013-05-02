import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class DatabaseLogin {

	public static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	public static String DB_URL = null;
	public static String USER = null;
	public static String PASS = null;	
	
	public static void uploadLogin() throws IOException
	{
		
		BufferedReader loginFile = new BufferedReader(new FileReader("../../databaseLogin.txt"));
		
		DatabaseLogin.USER = loginFile.readLine().split(" ")[1];
		DatabaseLogin.PASS = loginFile.readLine().split(" ")[1];
		String host = loginFile.readLine().split(" ")[1];
		String db = loginFile.readLine().split(" ")[1];
		String port = loginFile.readLine().split(" ")[1];
		DatabaseLogin.DB_URL = String.format( "jdbc:mysql://%s:%s/%s", host, port, db );
		
		loginFile.close();
		
	}
	
}
