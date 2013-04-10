import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;


public class ParserTest {

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";
	
	static final String USER = "vcf_user";
	static final String PASS = "vcf";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs;
		
		try {
	
			Class.forName(JDBC_DRIVER);
	
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
		}
		catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		
		String sql = "SELECT `VcfId` FROM `vcf_analyzer`.`Vcf` WHERE `VcfName`='testVCF_adam'";
		int vcfID;
		
		try {
			rs = stmt.executeQuery(sql);
			rs.next();
			
			vcfID = rs.getInt(1);
		} catch (SQLException se) {
			throw new SQLException(se.getMessage());
		}
		
		sql = String.format("SELECT `EntryId` FROM `vcf_analyzer`.`VcfEntry` WHERE `VcfId`='%s'", vcfID);
		
		try {
			rs = stmt.executeQuery(sql);
		} catch (SQLException se) {
			throw new SQLException(se.getMessage());
		}
		
		ArrayList<Integer> entryList = new ArrayList<Integer>();
		while (rs.next()) {
			entryList.add(rs.getInt("EntryId"));
		}
		
		ArrayList<Integer> individualList = new ArrayList<Integer>();
		for (Integer entry : entryList) {
			try {
				sql = String.format("SELECT `IndId` FROM `vcf_analyzer`.`IndividualEntry` WHERE `EntryId`='%s'", entry);
				rs = stmt.executeQuery(sql);
				
				while (rs.next()) {
					individualList.add(rs.getInt("IndId"));
				}
				
			} catch (SQLException se) {
				throw new SQLException(se.getMessage());
			}
		}
		
		ArrayList<String> infoFields = new ArrayList<String>(Arrays.asList("AD", "DP", "GQ", "PL"));

		for (String infoField : infoFields) {
			for (Integer ind : individualList) {
				sql = String.format("SELECT * FROM `vcf_analyzer`.`%s` WHERE `IndID`='%s'", infoField, ind);
				System.out.println(sql);
				try {
					rs = stmt.executeQuery(sql);
					rs.next();
					if (rs.getString("IndID") == null) { System.out.println(String.format("Error uploading %s", infoField)); return; }
				} catch (SQLException se) {
					throw new SQLException(se.getMessage());
				}
			}
		}
	}
}
