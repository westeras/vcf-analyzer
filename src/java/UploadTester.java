import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UploadTester {
	private Connection connection;
	private Statement stmt;
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";

	static final String USER = "vcf_user";
	static final String PASS = "vcf";

	@Before
	public void setUpTables() throws ClassNotFoundException, SQLException {
		Class.forName(JDBC_DRIVER);
		this.connection=DriverManager.getConnection(DB_URL, USER, PASS);
		this.stmt=this.connection.createStatement();
		this.stmt.executeUpdate("TRUNCATE TABLE `Annotation`");
		this.stmt.executeUpdate("TRUNCATE TABLE `Divergence`");
	}

	@Test
	public void testUploadAnnotation() throws SQLException, FileNotFoundException, ClassNotFoundException {
		Command uploadAnnotCommand=new UploadAnnotationCommand("Examples/annot.txt","","");
		AnnotationParser parser= new AnnotationParser(new File("Examples/annot.txt"));
		uploadAnnotCommand.execute();
		ResultSet rs=this.stmt.executeQuery("Select * from `Annotation`");
		ArrayList<String[]> correctAnswers= parser.parseFile();
		int i=0;
		while (rs.next()){
			String []resultRow={rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(rs.getString(5)),rs.getString(6),rs.getString(7)};
			assertEquals(correctAnswers.get(i)[0],resultRow[0]);
			assertEquals(correctAnswers.get(i)[1],resultRow[1]);
			assertEquals(correctAnswers.get(i)[2],resultRow[2]);
			assertEquals(correctAnswers.get(i)[3],resultRow[3]);
			assertEquals(correctAnswers.get(i)[4],resultRow[4]);
			assertEquals(correctAnswers.get(i)[5],resultRow[5]);
			assertEquals(correctAnswers.get(i)[6],resultRow[6]);
		}
		rs.close();
		
		
	}
		
	@After
	public void cleanUpConnection() throws SQLException {
		if (this.connection != null) {
			this.connection.close();
		}
		if (this.stmt != null) {
			this.stmt.close();
		}
	}
}
