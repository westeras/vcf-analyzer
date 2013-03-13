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
    
    public DatabaseConnector() throws SQLException
    {
        Connection conn = null;
        Statement stmt = null;

        Class.forName(JDBC_DRIVER);

        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        stmt = conn.createStatement();
        stmt.close();
        conn.close();
    
    }
    
    public CloseConnection()
    {
        if (conn!=null) 
        {
            conn.close();
        }
        if (stmt!=null)
        {
            stmt.close();
        }
    }
    
    public long getVcfId( String vcfName)
    {
        String sql = null;
        try
        {
            sql = "SELECT `VcfId` FROM `vcf_analyzer`.`Vcf` WHERE `VcfName` = '" + vcfName +"'";
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) 
            {
                int id = Long.parseLong( rs.getString("VcfId") );
                rs.close();
                return id;
            }
        
            throw new IllegalArgumentException("VCF: " + vcfName + " not found");
            
        } catch(SQLException se) {
            throw new SQLException("Invadild Query" + sql);
        }
   
        return -1
    }

}