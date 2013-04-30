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
	private int numOnes;
	private int numZeros;
	public DivergenceSummary(String vcf_name,String div_name, String filter) throws ClassNotFoundException, SQLException{
		this.numOnes=0;
		this.numZeros=0;
		this.vcf_name=vcf_name;
		this.div_name=div_name;
		this.filter=filter;				
	}
	
	@Override
	public String execute() {
		
		String sql= buildSQLStatment();
		try {
			this.conn=new DatabaseConnector();
			ResultSet tuples=this.conn.executeQuery(sql);
			ArrayList<String> results=convertToArrayList(tuples); 
			count(results);
			String summary=printSummary();
			System.out.println(summary);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		return null;
	}
	
	
	
	String printSummary() {
		String toRet="0s: "+this.numZeros+"\n"+
					"1s: "+this.numOnes+"\n";
		return toRet;
		
	}

	private void count(ArrayList<String> results) {
		for (String s: results){
			System.out.println(s);
			if (s.equals("1")){
				this.numOnes++;
			}
			else if (s.equals("0")){
				this.numZeros++;
			}
		}
		
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
		if (this.filter.equals("")){
		return "Select `DivValue` from `Vcf`, `VcfEntry`, `Divergence`  "+
				"where `Vcf`.`VcfName`='"+this.vcf_name+"' and `Vcf`.`VcfId`=`VcfEntry`.`VcfId` and"+
				" `VcfEntry`.`Chrom`= `Divergence`.`Chromosome`"+
				" and `VcfEntry`.`Pos`= `Divergence`.`Position` and `Divergence`.`DivName`='"+this.div_name+"'";
		}
		else {
			return "Select `DivValue` from `Vcf`, `VcfEntry`, `Divergence`, `Filter`, `FilterEntryPass` "+
					"where `Vcf`.`VcfName`='"+this.vcf_name+"' and `Vcf`.`VcfId`=`VcfEntry`.`VcfId` and"+
					" `VcfEntry`.`Chrom`= `Divergence`.`Chromosome`"+
					" and `VcfEntry`.`Pos`= `Divergence`.`Position` and `Divergence`.`DivName`='"+this.div_name+"' "+
					"and `Filter`.`FilName`='"+this.filter+"' and `Filter`.`FilId`=`FilterEntryPass`.`FilId` and `FilterEntryPass`.`Pass`=1 and `VcfEntry`.`EntryId`=`FilterEntryPass`.`EntryId`";				
		}
		

	}

	@Override
	public void pipeOutput() {
		// TODO Auto-generated method stub.

	}


}
