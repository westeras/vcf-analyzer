import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;


public class FilterCreator {
	String[] commandList;
	DatabaseConnector dbConnector;
	int filterID;
	private static ArrayList<String> operatorList = new ArrayList<String>(Arrays.asList("<", ">", "<=", ">=", "="));
	
	public FilterCreator(String filterName, String[] commandList) throws ClassNotFoundException, SQLException {
		this.commandList = commandList;
		dbConnector = new DatabaseConnector();
		
		this.filterID = dbConnector.createFilter(filterName);
		
		for (int i = 0; i < this.commandList.length; i++) {
			parseCommand(i);
		}
	}
	
	private void parseCommand(int index) throws SQLException {
		String currentCommand = this.commandList[index];
		for (int i = 0; i < operatorList.size(); i++) {
			if (currentCommand.contains(operatorList.get(i))) {
				String[] arguments = currentCommand.split(operatorList.get(i));
				String[] operands = arguments[1].split(" ");
				dbConnector.createFilterEntry(this.filterID, i, arguments[0], operands);
			}
		}
	}
}
