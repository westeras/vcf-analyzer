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
		this.connection = DriverManager.getConnection(DB_URL, USER, PASS);
		this.stmt = this.connection.createStatement();
		this.stmt.executeUpdate("TRUNCATE TABLE `Annotation`");
		this.stmt.executeUpdate("TRUNCATE TABLE `Divergence`");
	}

	@Test
	public void testUploadAnnotation() throws SQLException,
			FileNotFoundException, ClassNotFoundException {
		Command uploadAnnotCommand = new UploadAnnotationCommand(
				"Examples/annot.txt", "", "");
		AnnotationParser parser = new AnnotationParser(new File(
				"Examples/annot.txt"));
		uploadAnnotCommand.execute();
		ResultSet rs = this.stmt.executeQuery("Select * from `Annotation`");
		ArrayList<String[]> correctAnswers = parser.parseFile();
		int i = 0;
		while (rs.next()) {
			String[] resultRow = { rs.getString("Chromosome"),
					String.valueOf(rs.getInt("StartPosition")),
					String.valueOf(rs.getInt("EndPosition")),
					rs.getString("GeneName"), rs.getString("GeneDirection") };
			for (int j = 0; j < 5; j++) {
				assertEquals(correctAnswers.get(i)[j], resultRow[j]);
			}
			i++;
		}
		rs.close();
	}

	@Test
	public void testUploadDivergence() throws SQLException,
			FileNotFoundException, ClassNotFoundException {
		Command uploadDivCommand = new UploadDivergenceCommand(
				"Examples/divergence.txt", "", "");
		DivergenceParser parser = new DivergenceParser(new File(
				"Examples/divergence.txt"));
		uploadDivCommand.execute();
		ResultSet rs = this.stmt.executeQuery("Select * from `Divergence`");
		ArrayList<String[]> correctAnswers = parser.parseFile();
		int i = 0;
		while (rs.next()) {
			String[] resultRow = { rs.getString("Chromosome"),
					String.valueOf(rs.getInt("Position")),
					String.valueOf(rs.getInt("DivValue")) };
			for (int j = 0; j < 3; j++) {
				assertEquals(correctAnswers.get(i)[j], resultRow[j]);
			}
			i++;
		}
		rs.close();
	}

	@After
	public void cleanUpConnection() throws SQLException {
		this.stmt.executeUpdate("TRUNCATE TABLE `Divergence` ");
		this.stmt.executeUpdate("TRUNCATE TABLE `Annotation` ");
		this.connection.close();
		this.stmt.close();
	}
}
