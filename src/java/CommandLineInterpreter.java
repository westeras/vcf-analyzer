import org.apache.commons.cli.CommandLine;  
import org.apache.commons.cli.CommandLineParser;  
import org.apache.commons.cli.GnuParser;  
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;  
import org.apache.commons.cli.ParseException;
  
public class CommandLineInterpreter
{  
	/**
     * Use GNU Parser
	 * Interprets commands given and carries out the proper functions.
	 * If you want to add a command, add it here as an if statement and
	 * in the constructGnuOptions function for the program to recognize it.
	 * @param commandLineArguments
	 */

	public static String useGnuParser(final String[] commandLineArguments){  
		final CommandLineParser cmdLineGnuParser = new GnuParser();  
  
		final Options gnuOptions = new CommandLineConstructors().getCommands();  
		CommandLine commandLine;  
		
		String result = "";
		
		try{  
			commandLine = cmdLineGnuParser.parse(gnuOptions, commandLineArguments);
			
			if (commandLine.hasOption("updiv")){
				result = uploadCommand(commandLineArguments, commandLine, "updiv");
			}
			
			if (commandLine.hasOption("upano")){
				result = uploadCommand(commandLineArguments, commandLine, "upano");
			}
			
			if (commandLine.hasOption("asf")){
				
			}
			
			if (commandLine.hasOption("sum")){
				
				String[] stringNumbers = commandLine.getOptionValues("sum");
				int sum = 0;

				for(int i = 0; i < stringNumbers.length; i++){
					sum += Integer.parseInt(stringNumbers[i]);
				}
        	 
				System.out.println(sum);
			}
			
			if (commandLine.hasOption("help")){
				
				/*
				 * Expand to a more general help function
				 */
				
				System.out.println("hello\nn <arg>\nsum <arg0> <arg1> <arg2> ...");
			}
		}
      
		catch (ParseException parsingException){  
			System.err.println("Could not find argument: " + parsingException.getMessage());  
		}
		
		return result;
	}  
	
	/**
	 * Uploads either divergence file or annotation file
	 * @param commandLineArguments
	 * @param commandLine
	 * @param type
	 * @return the name of the upload
	 */
	
	public static String uploadCommand(final String[] commandLineArguments, CommandLine commandLine, String type){
		String[] args;
		Command command = null;
		
		if(type == "updiv"){
			args = commandLine.getOptionValues("updiv");
		}else if(type == "upano"){
			args  = commandLine.getOptionValues("upano");
		}else{
			args = null;
		}
		
		String result = "";
		
		if(args == null){
			result = "Please input arguments";
		}else if(args.length == 1){
			if(type=="updiv")command = new UploadDivergenceCommand(commandLine.getOptionValues(type)[0], null,"");
			if(type=="upano")command = new UploadAnnotationCommand(commandLine.getOptionValues(type)[0], null,"");
			
			result = command.execute();
		}else if(args.length == 2){
			if(type=="updiv")command = new UploadDivergenceCommand(commandLine.getOptionValues(type)[0], null,commandLine.getOptionValues(type)[1]);
			if(type=="upano")command = new UploadAnnotationCommand(commandLine.getOptionValues(type)[0], null,commandLine.getOptionValues(type)[1]);
			
			result = command.execute();
		}else{
			result = "Incorrect number of arguments";
		}
		
		return result;
	}
  

   /**
    * Prints out the commands the user input.
    */
   
   public static void displayInput(final String[] commandLineArguments){  
	   
	   int length = commandLineArguments.length;
	   String output = "";
	   
	   for(int i = 0; i < length; i++){
		   output += commandLineArguments[i];
		   output += " ";
	   }
	   
	   System.out.println(output);
   }
   
   /**
    * This is the method that should be called by outside methods and classes
    * to run all commands.
    */
   
   public static String interpreter(String[] commandLineArguments){
	    if (commandLineArguments.length < 1)  
	      {  
	         System.out.println("Please input help"); 
	      }  
	      displayInput(commandLineArguments);
	      System.out.println("");
	      return useGnuParser(commandLineArguments);  
   }
  
   /** 
    * Main executable method used to demonstrate Apache Commons CLI. 
    *  
    * @param commandLineArguments Commmand-line arguments. 
    */  
   public static void main(final String[] commandLineInput){  
	   System.out.println("Test Parser");
	   System.out.println("Developed for the Gene-E project\n");
	   
	   interpreter(commandLineInput);
   }  
} 
