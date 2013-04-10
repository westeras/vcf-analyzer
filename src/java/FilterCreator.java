import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// ########### What does this guy do?
/**
 * Talk about it here!
**/
public class FilterCreator {
	String[] commandList;
	DatabaseConnector dbConnector;
	int filterID;
	private HashMap<String, Integer> operatorList;
	
	public FilterCreator(String filterName, String[] commandList) throws ClassNotFoundException, SQLException {
		this.commandList = commandList;
		dbConnector = new DatabaseConnector();
		fillOperatorList();
		
		this.filterID = dbConnector.createFilter(filterName);
	}

	protected void uploadEntries() throws SQLException {
		for (int i = 0; i < this.commandList.length; i++) {
			parseCommand(i);
		}
	}
	
	private void parseCommand(int index) throws SQLException {
		String currentCommand = this.commandList[index];
		ArrayList<String> infoNames = dbConnector.getInfoTableNames();
		ArrayList<String> genoNames = dbConnector.getGenotypeTableNames();
		ArrayList<String> indNames = new ArrayList<String>(Arrays.asList("ind", "IND", "Ind", "individual", "in"));
		ArrayList<String> entryNames = new ArrayList<String>(Arrays.asList("entry", "ENT", "ent"));
		
		for (String key : this.operatorList.keySet()) {
			if (currentCommand.contains(key)) {
				String[] arguments = currentCommand.split(key);
				trimAllArguments(arguments);
				String[] operands = arguments[1].split(" ");
				String[] identifiers = arguments[0].split(" ");
				
				if (infoNames.contains(identifiers[1]) && indNames.contains(identifiers[0])) {
					dbConnector.createIndividualEntry(this.filterID, this.operatorList.get(key), identifiers[1], operands);
				} else if (genoNames.contains(identifiers[1]) && entryNames.contains(identifiers[0])) {
					dbConnector.createFilterEntry(this.filterID, this.operatorList.get(key), identifiers[1], operands);
				} else {
					System.out.println("Invalid info name or genotype name");
				}
			}
		}
	}

	private void trimAllArguments(String[] arguments) {
		for (int i = 0; i < arguments.length; i++) { 
			arguments[i] = arguments[i].trim(); 
		}
	}
	
	private void fillOperatorList() {
		this.operatorList = new HashMap<String, Integer>();
		this.operatorList.put("<", 0);
		this.operatorList.put("less than", 0);
		this.operatorList.put(">", 1);
		this.operatorList.put("greater than", 1);
		this.operatorList.put("<=", 2);
		this.operatorList.put(">=", 3);
		this.operatorList.put("=", 4);
		this.operatorList.put("equal to", 4);
		this.operatorList.put("between", 5);
	}
	
	
}
