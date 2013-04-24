import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Apr 17, 2013.
 */
public class DivergenceSummary extends Command {

	private String vcf_name;
	private String div_name;
	private String filter; 
	private DatabaseConnector conn;
	public DivergenceSummary(String vcf_name,String div_name, String filter) throws ClassNotFoundException, SQLException{
		this.conn=new DatabaseConnector();
		this.vcf_name=vcf_name;
		this.div_name=div_name;
		this.filter=filter;				
	}
	
	@Override
	public String execute() {
		String sql= buildSQLStatment();
		int summary;
		try {
			ResultSet tuples=this.conn.executeQuery(sql);
			ArrayList<String> results=convertToArrayList(tuples); 
			if (this.filter!=""){
				results=applyFilter(results);
			}
			summary=count(results);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		return null;
	}
	
	
	
	private int count(ArrayList<String> results) {
		// TODO Auto-generated method stub.
		return 0;
	}

	private ArrayList<String> applyFilter(ArrayList<String> results) {
		// TODO Auto-generated method stub.
		return null;
	}

	private ArrayList<String> convertToArrayList(ResultSet r)
			throws SQLException {
		ArrayList<String> result = new ArrayList<String>();
		while (r.next()) {
			result.add(r.getString(1));
		}
		r.close();
		return result;
	}

	
	protected String buildSQLStatment() {
		String sql = "Select `DivValue` from `Vcf`, `VcfEntry`, `Divergence`  "+
				"where `Vcf`.`VcfName`='"+this.vcf_name+"' and `Vcf`.`VcfId`=`VcfEntry`.`VcfId` and"+
				"`VcfEntry`.`VcfId`=`Divergence`.`VcfId` and `VcfEntry`.`Chrom`= `Divergence`.`Chromosome`"+
				" and `VcfEntry`.`Pos`= `Divergence`.`Position` and `Divergence`.`DivName`='"+this.div_name+"'";
		return sql;
	}

	@Override
	public void pipeOutput() {
		// TODO Auto-generated method stub.

	}


}
