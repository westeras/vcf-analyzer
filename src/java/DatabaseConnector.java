import java.sql.*;
import javax.sql.*;
import java.util.Properties;

class DatabaseConnector
{
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/vcf_analyzer";
    
    static final String USER = "vcf_user";
    static final String PASS = "vcf";
    
    private Connection conn;
    private Statement stmt;
    
    public DatabaseConnector() throws SQLException, ClassNotFoundException
    {
        this.conn = null;
        this.stmt = null;

        Class.forName(JDBC_DRIVER);

        conn = DriverManager.getConnection(DB_URL, USER, PASS);
    
    }
    
    public long getVcfId( String vcfName) throws IllegalArgumentException, SQLException
    {
        String sql = null;
        try
        {
            sql = "SELECT `VcfId` FROM `vcf_analyzer`.`Vcf` WHERE `VcfName` = '" + vcfName +"'";
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) 
            {
                long id = Long.parseLong( rs.getString("VcfId") );
                rs.close();
                return id;
            }
        
            throw new IllegalArgumentException("VCF: " + vcfName + " not found");
            
        } catch(SQLException se) {
            throw new SQLException("Invalid Query: " + sql);
        }
    }
    
    public String getVcfHeader( String vcfId) throws IllegalArgumentException, SQLException
    {
        String sql = null;
        try
        {
            sql = "SELECT `VcfHeader` FROM `vcf_analyzer`.`Vcf` WHERE `VcfId` = '" + vcfId +"'";
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) 
            {
                String result = rs.getString("Header");
                rs.close();
                return result;
            }
        
            throw new IllegalArgumentException("VCF header for: " + vcfId + " not found");
            
        } catch(SQLException se) {
            throw new SQLException("Invalid Query: " + sql);
        }
    }
	
	public int getFilterID(String filterName) throws IllegalArgumentException, SQLException {
		String sql = null;
		try {
			sql = "SELECT `FilID` FROM `vcf_analyzer`.`Filter` WHERE `FilName` = '" + filterName + "'";
			ResultSet rs = stmt.executeQuery(sql);
			
			if (rs.next()) {
				int id = Integer.parseInt(rs.getString("FilId"));
				rs.close();
				return id;
			}
			
			throw new IllegalArgumentException("Filter: " + filterName + " not found");
		} catch(SQLException se) {
			throw new SQLException("Invalid Query: " + sql);
		}
	}
    
    public void CloseConnection() throws SQLException {
		if (this.conn != null) {
			this.conn.close();
		}
		if (this.stmt != null) {
			this.stmt.close();
		}
	}

	private boolean hasOpenStatementAndConnection() throws SQLException {
		return !this.conn.isClosed() && !this.stmt.isClosed();
	}

	private void reopenConnectionAndStatement() throws SQLException,
			ClassNotFoundException {
		if (this.conn == null || this.conn.isClosed())
			this.conn = DriverManager.getConnection(DB_URL, USER, PASS);
		if (this.stmt == null || this.stmt.isClosed())
			this.stmt = this.conn.createStatement();
	}

	protected void uploadDivergence(String name) throws ClassNotFoundException,
			SQLException {
		if (!hasOpenStatementAndConnection())
			reopenConnectionAndStatement();
		String sql = String.format(
				"INSERT into Divergence (DivName) VALUES (`%s`);", name);
		ResultSet rs = this.stmt.executeQuery(sql);
		while (rs.next()) {
			System.out.println(rs.toString());
		}
	}

	/**
	 * 
	 * TODO consider refactoring to one general upload method
	 * 
	 * @param chromosome
	 * @param position
	 * @param divValue
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected void uploadDivergenceLine(String chromosome, int position,
			int divValue) throws ClassNotFoundException, SQLException {
		if (!hasOpenStatementAndConnection())
			reopenConnectionAndStatement();
		String sql = String
				.format("INSERT into DivergenceLine (DivId, Chromosome, Position, DivValue) VALUES (`%s`,`%s`,`%s`,`%s` );",
						getHighestId("Divergence","DivId"), chromosome, position, divValue);
		ResultSet rs = this.stmt.executeQuery(sql);
		while (rs.next()) {
			System.out.println(rs.toString());
		}
	}

	/**
	 * TODO: Finish Testing
	 * @param tableName, String idName 
	 * 
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private int getHighestId(String tableName, String idName) throws ClassNotFoundException, SQLException {
		if (!hasOpenStatementAndConnection())
			reopenConnectionAndStatement();
		String sql = String.format("SELECT %s FROM %s ORDER BY %s desc LIMIT 0,1;",idName,tableName,idName);
		ResultSet rs = this.stmt.executeQuery(sql);
		return Integer.parseInt(rs.getString("DivID"));
	}

	protected void uploadAnnotation(String name) throws ClassNotFoundException,
			SQLException {
		if (!hasOpenStatementAndConnection())
			reopenConnectionAndStatement();
		String sql = String.format(
				"INSERT into Annotation (AnnoName) VALUES (`%s`);", name);
		ResultSet rs = this.stmt.executeQuery(sql);
		while (rs.next()) {
			System.out.println(rs.toString());
		}
	}

	/**
	 * 
	 * TODO consider refactoring to one general upload method
	 * 
	 * @param chromosome
	 * @param startPosition
	 * @param geneDirection
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected void uploadAnnotationLine(String chromosome, int startPosition, int endPosition,
			String geneName, int geneDirection) throws ClassNotFoundException, SQLException {
		if (!hasOpenStatementAndConnection())
			reopenConnectionAndStatement();
		String sql = String
				.format("INSERT into AnnotationLine (AnnoID, Chromosome, StartPosition, EndPosition, GeneName, GeneDirection) VALUES (`%i`,`%s`,`%i`,`%i`,`%s`,`%i` );",
						getHighestId("Annotation","AnnoLineID"), chromosome, startPosition, endPosition, geneName, geneDirection);
		ResultSet rs = this.stmt.executeQuery(sql);
		while (rs.next()) {
			System.out.println(rs.toString());
		}
	}

}