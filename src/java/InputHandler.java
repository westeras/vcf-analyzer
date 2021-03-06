import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

public class InputHandler {

	/**
	 * Takes input and runs it through the command interpreter.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] arg) throws ClassNotFoundException, SQLException {
		Scanner input = new Scanner(System.in);
		String[] commands = null;
		System.out.println("Please input a command or type in 'help' for a list of commands.");
		while(true){
			System.out.print("> ");
		
			String allLine = input.nextLine();
			try {
			    commands = allLine.split("\\s+");
			} catch (PatternSyntaxException ex) {
			    System.out.println("Error: " + ex);
			}
			
			if(commands[0].equals("quit")){
				break;
			}else{				
				System.out.println(CommandLineInterpreter.interpreter(commands));
			}
		}
		
		input.close();
	}

}
