import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;


public abstract class UploadCommand extends Command {
	public abstract ArrayList<String[]> parseFile() throws FileNotFoundException;
	public abstract String getSQLStatement(ArrayList<String[]> rowsToUpload) ;
	
	public String upload(File file) throws ClassNotFoundException, SQLException, FileNotFoundException{
		DatabaseConnector connection=new DatabaseConnector();
		ArrayList<String[]> rowsToUpload=parseFile();
		String sql= getSQLStatement(rowsToUpload);
		connection.upload(sql);
		return file.toString()+" Uploaded Successfully!";
	}
	
}
