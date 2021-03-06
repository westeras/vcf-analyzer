package commandLine;

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

	public static void useGnuParser(final String[] commandLineArguments){  
		final CommandLineParser cmdLineGnuParser = new GnuParser();  
  
		final Options gnuOptions = constructGnuOptions();  
		CommandLine commandLine;  
		try{  
			commandLine = cmdLineGnuParser.parse(gnuOptions, commandLineArguments);
			
			if (commandLine.hasOption("hello")){  
				System.out.println("Hello world!");  
			}
			
			if (commandLine.hasOption("n")){  
				System.out.println("You input this " + commandLine.getOptionValue("n"));  
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
	}  
  
	/**
	 * Constructs the actual commands.
	 * @return
	 */
	
   public static Options constructGnuOptions(){ 
      final Options gnuOptions = new Options();  
      gnuOptions.addOption("hello", "helloWorld", false, "Test function")
                .addOption("n", true, "Returns same value")
                .addOption(constructSumOption())
                .addOption("help", false, "Tells the user what functions there are.");
      return gnuOptions;  
   } 
   
   /**
    * Constructs the sum command.
    * @return
    */
   public static Option constructSumOption(){
	   @SuppressWarnings("static-access")
	   Option sum = OptionBuilder
			   .withArgName("addends")
			   .hasArgs()
			   .withDescription("Returns the sum of the inputted integers")
			   .create("sum");
	   return sum;
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
   
   public static void interpreter(String[] commandLineArguments){
	    if (commandLineArguments.length < 1)  
	      {  
	         System.out.println("Please input help"); 
	      }  
	      displayInput(commandLineArguments);
	      System.out.println("");
	      useGnuParser(commandLineArguments);  
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
