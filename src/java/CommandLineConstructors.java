import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


public class CommandLineConstructors {
	
	Options listOfCommands;
	
	public CommandLineConstructors(){
		this.listOfCommands = constructGnuOptions();
	}
	
	public Options getCommands(){
		return listOfCommands;
	}

	/**
	 * Constructs the actual commands.
	 * @return
	 */
	
   public static Options constructGnuOptions(){ 
      final Options gnuOptions = new Options();  
      gnuOptions.addOption(constructUpDiv())
                .addOption(constructSumOption())
                .addOption(constructUpAno())
                .addOption(constructAFS())
                .addOption("help", false, "Tells the user what functions there are.");
      return gnuOptions;  
   } 
   
   /**
    * Constructs the sum command.
    * @return
    */
   private static Option constructSumOption(){
	   @SuppressWarnings("static-access")
	   Option sum = OptionBuilder
			   .withArgName("addends")
			   .hasArgs()
			   .withDescription("Returns the sum of the inputted integers")
			   .create("sum");
	   return sum;
   }
   
   /**
    * Constructs the upload divergence command.
    */
   
   private static Option constructUpDiv(){
	   @SuppressWarnings("static-access")
	   Option updiv = OptionBuilder
	   			.withArgName("File Location")
	   			.hasArg()
	   			.withArgName("Name")
	   			.hasOptionalArg()
	   			.withDescription("Uploads a Divergence file")
	   			.create("updiv");
	   
	   return updiv;
   }
   
   /**
    * Constructs the upload annotation command.
    */
   
   private static Option constructUpAno(){
	   @SuppressWarnings("static-access")
	   Option upano = OptionBuilder
	   			.withArgName("File Location")
	   			.hasArg()
	   			.withArgName("Name")
	   			.hasOptionalArg()
	   			.withDescription("Uploads a Annotation file")
	   			.create("upano");
	   
	   return upano;
   }
   
   /**
    * Constructs the allele frequency spectra command.
    */
   
   private static Option constructAFS(){
	   @SuppressWarnings("static-access")
	   Option afs = OptionBuilder
	   			.withArgName("VCF File")
	   			.hasArg()
	   			.withArgName("Filter")
	   			.hasOptionalArg()
	   			.withDescription("Calls Allele Frequency Spectra")
	   			.create("afs");
	   
	   return afs;
   }
  

}
