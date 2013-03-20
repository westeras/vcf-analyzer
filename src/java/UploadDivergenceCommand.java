import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Mar 14, 2013.
 */
public class UploadDivergenceCommand extends Command{
	private File fileLocation;
	private String options;
	
	public UploadDivergenceCommand(String fileLocation, String options){
		this.fileLocation=new File(fileLocation);
		this.options=options;
	}
	@Override
	public void execute() {
		try {
			DatabaseConnector connection=new DatabaseConnector();
			DivergenceParser parser=new DivergenceParser(this.fileLocation);
			ArrayList<String[]> rowsToUpload=parser.parseFile();
			for (String[] row : rowsToUpload){
				connection.uploadDivergenceLine(row[0], Integer.valueOf(row[1]),Integer.valueOf(row[2]));
			}			
		} catch (ClassNotFoundException exception) {
			// TODO Auto-generated catch-block stub.
			exception.printStackTrace();
		} catch (SQLException exception) {
			// TODO Auto-generated catch-block stub.
			exception.printStackTrace();
		} catch (FileNotFoundException exception) {
			// TODO Auto-generated catch-block stub.
			exception.printStackTrace();
		}
		
		
	}

	@Override
	public void pipeOutput() {
		// TODO Auto-generated method stub.
		
	}

}
