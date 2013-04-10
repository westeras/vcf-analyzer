
/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Apr 10, 2013.
 */
public class AFSCommand extends Command{
	private String options;
	private String VCFName;
	
	public AFSCommand(String VCFName, String options){
		this.VCFName=VCFName;
		this.options=options;
	}
	@Override
	public String execute() {
		// TODO Auto-generated method stub.
		return null;
	}

	@Override
	public void pipeOutput() {
		// TODO Auto-generated method stub.
		
	}

}
