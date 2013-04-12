import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * TODO Put here a description of what this class does.
 * 
 * @author schepedw. Created Mar 14, 2013.
 */
public class UploadDivergenceCommand extends Command {
	private File fileLocation;
	private String options;
	private String name;

	public UploadDivergenceCommand(String fileLocation, String options,
			String name) {
		this.fileLocation = new File(fileLocation);
		this.options = options;
		this.name = name;
		if (this.name == "") {
			this.name = getDate();
		}
	}

	/**
	 * TODO Put here a description of what this constructor does.
	 * 
	 * @param fileLocation2
	 * @param options2
	 * @return
	 */

	@Override
	public String execute() {
		String resultStmnt = this.fileLocation.toString()
				+ " Uploaded Successfully!";
		try {
			DatabaseConnector connection = new DatabaseConnector();
			DivergenceParser parser = new DivergenceParser(this.fileLocation);
			ArrayList<String[]> rowsToUpload = parser.parseFile();
			for (String[] row : rowsToUpload) {
				String chromosome = row[0];
				int position = Integer.valueOf(row[1]);
				int divValue = Integer.valueOf(row[2]);
				String sql = String
						.format("INSERT into `Divergence` (`DivName`, `Chromosome`, `Position`, `DivValue`) VALUES ('%s','%s','%d','%d');",
								this.name, chromosome, position, divValue);
				connection.upload(sql);
			}
		} catch (Exception exception) {
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

}
