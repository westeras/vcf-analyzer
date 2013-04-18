
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
	public DivergenceSummary(String vcf_name,String div_name, String filter){
		this.vcf_name=vcf_name;
		this.div_name=div_name;
		this.filter=filter;				
	}
	
	@Override
	public String execute() {
		String sql= buildSQLStatment();
		return null;
	}

	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @return
	 */
	private String buildSQLStatment() {
		String sql = "Select `DivValue` from `Vcf`, `VcfEntry`, `Divergence`  "+
				"where `VcfName`"
		return ;
	}

	@Override
	public void pipeOutput() {
		// TODO Auto-generated method stub.

	}


}
