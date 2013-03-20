import java.io.File;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Mar 14, 2013.
 */
public class UploadAnnotationCommand extends Command{
	private File fileLocation;
	private String options;
	public UploadAnnotationCommand(String fileLocation, String options){
		this.fileLocation=new File(fileLocation);
		this.options=options;
	}
	@Override
	public void execute() {
		// TODO Auto-generated method stub.
		
	}

	@Override
	public void pipeOutput() {
		// TODO Auto-generated method stub.
		
	}
	/**
	 * TODO: implement
	 */

}
