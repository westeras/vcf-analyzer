package commandLine;

import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

public class InputHandler {

	/**
	 * Takes input and runs it through the command interpreter.
	 */
	public static void main(String[] arg) {
		Scanner input = new Scanner(System.in);
		String[] commands = null;
		System.out.println("Please input a command or type in '-help' for a list of commands.");
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
				commands[0] = "-" + commands[0];
				
				CommandLineInterpreter.interpreter(commands);
			}
		}
		
		input.close();
	}

}
