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
public class UploadAnnotationCommand extends Command{
	private File fileLocation;
	private String options;
	private String name;
	public UploadAnnotationCommand(String fileLocation, String options, String name){
		this.fileLocation=new File(fileLocation);
		this.options=options;
		this.name=name;
		if (this.name==""){
			this.name=getDate();
		}
	}
	
	/*
	 * TODO: figure out how to tell if the upload was successful
	 */
	
	@Override
	public String execute() {
		String resultStmnt=this.fileLocation.toString()+" Uploaded Successfully!";
	 try {
		DatabaseConnector connection=new DatabaseConnector();
		AnnotationParser parser=new AnnotationParser(this.fileLocation);
		ArrayList<String[]> rowsToUpload=parser.parseFile();
		
		for (String[] row : rowsToUpload){
			String chromosome =row[0];
			String startPosition=row[1];
			String endPosition=row[2];
			String geneName=row[3];
			String geneDirection=row[4];
			String sql = String
					.format("INSERT into `Annotation` (`Chromosome`, `StartPosition`, `EndPosition`, `GeneName`, `GeneDirection`, `AnnoName`) VALUES ('%s','%d','%d','%s','%s','%s');",
							chromosome, startPosition, endPosition, geneName,
							geneDirection, this.name);
			connection.upload(sql);
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
	 System.out.println(resultStmnt);
	 return resultStmnt;
	
		
	}

	@Override
	public void pipeOutput() {
		// TODO Auto-generated method stub.
		
	}
	/**
	 * TODO: implement
	 */

}
