import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class UploadAnnotationCommand extends UploadCommand {
	private File fileLocation;
	private String options;
	private String name;

	public UploadAnnotationCommand(String fileLocation, String options,
			String name) {
		this.fileLocation = new File(fileLocation);
		this.options = options;
		this.name = name;
		if (this.name == "") {
			this.name = getDate();
		}
	}

	@Override
	public String execute() {

		try {
			String result = upload(this.fileLocation);
			return result;

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return "Upload Failed!";
	}

	@Override
	public ArrayList<String[]> parseFile() throws FileNotFoundException {
		FileInputStream fileIn = new FileInputStream(this.fileLocation);
		Scanner reader = new Scanner(fileIn);
		ArrayList<String[]> rows = new ArrayList<String[]>();
		while (reader.hasNextLine()) {
			String[] columns = parseLine(reader.nextLine());
			rows.add(columns);
		}
		return rows;
	}

	private String[] parseLine(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		String chrom = tokenizer.nextToken();
		String startPosition = tokenizer.nextToken();
		String endPosition = tokenizer.nextToken();
		String geneName = tokenizer.nextToken();
		String geneDirection = tokenizer.nextToken();
		return new String[] { chrom, startPosition, endPosition, geneName,
				geneDirection };
	}

	@Override
	public String getSQLStatement(String[] row) {
		String chromosome = row[0];
		int startPosition = Integer.valueOf(row[1]);
		int endPosition = Integer.valueOf(row[2]);
		String geneName = row[3];
		String geneDirection = row[4];
		String sql = String
				.format("INSERT into `Annotation` (`Chromosome`, `StartPosition`, "
						+ "`EndPosition`, `GeneName`, `GeneDirection`, `AnnoName`) VALUES ('%s','%d','%d','%s','%s','%s'); ",
						chromosome, startPosition, endPosition, geneName,
						geneDirection, this.name);

		return sql;
	}

	@Override
	public void pipeOutput() {
		// TODO Auto-generated method stub.

	}
}
