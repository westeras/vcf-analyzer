import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Mar 14, 2013.
 */
public abstract class Command {
	public abstract String execute();
	public abstract void pipeOutput();
	public abstract ArrayList<String[]> parseFile() throws FileNotFoundException;
	public abstract String getSQLStatement(ArrayList<String[]> rowsToUpload) ;


	protected String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	
	public String upload(File file) throws ClassNotFoundException, SQLException, FileNotFoundException{
		DatabaseConnector connection=new DatabaseConnector();
		ArrayList<String[]> rowsToUpload=parseFile();
		String sql= getSQLStatement(rowsToUpload);
		connection.upload(sql);
		return file.toString()+" Uploaded Successfully!";
	}
	
}
