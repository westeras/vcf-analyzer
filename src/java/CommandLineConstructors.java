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
                .addOption(constructFilterWrite())
                .addOption(constructFilterStore())
                .addOption(constructFilterCreate())
                .addOption(constructView())
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
    * Constructs the filter creator command.
    * @return
    */
   private static Option constructFilterCreate(){
	   @SuppressWarnings("static-access")
	   Option sum = OptionBuilder
			   .withArgName("filtername")
			   .hasArg()
			   .withArgName("arguments")
			   .hasOptionalArgs()
			   .withDescription("Creates a filter")
			   .create("createfilter");
	   return sum;
   }
   
   /**
    * Constructs the upload divergence command.
    */
   
   private static Option constructUpDiv(){
	   @SuppressWarnings("static-access")
	   Option updiv = OptionBuilder
	   			.withLongOpt("uploaddivergence")
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
	   			.withLongOpt("uploadannotation")
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
			    .withLongOpt("allelefrequencyspectra")
	   			.withArgName("VCF File")
	   			.hasArg()
	   			.withArgName("Filter")
	   			.hasOptionalArg()
	   			.withDescription("Calls Allele Frequency Spectra")
	   			.create("afs");
	   
	   return afs;
   }
  

   /**
    * Constructs the filter write applier command.
    */
   
   private static Option constructFilterWrite(){
	   @SuppressWarnings("static-access")
	   Option filterWrite = OptionBuilder
			    .withLongOpt("applyandwritefilter")
	   			.withArgName("VCF Name")
	   			.hasArg()
	   			.withArgName("Filter Name")
	   			.hasArg()
	   			.withArgName("File Name")
	   			.hasOptionalArg()
	   			.withDescription("Applies a filter to a vcf file and writes it to a file.")
	   			.create("filterwrite");
	   
	   return filterWrite;
   }
   
   /**
    * Constructs the filter store applier command.
    */
   
   private static Option constructFilterStore(){
	   @SuppressWarnings("static-access")
	   Option filterStore = OptionBuilder
			    .withLongOpt("filterstore")
	   			.withArgName("VCF Name")
	   			.hasArg()
	   			.withArgName("Filter Name")
	   			.hasArg()
	   			.withArgName("File Name")
	   			.hasOptionalArg()
	   			.withDescription("Applies a filter to a vcf file and stores it to a file.")
	   			.create("filterstore");
	   
	   return filterStore;
   }
   
   /**
    * Constructs the view command.
    */
   
   private static Option constructView(){
	   @SuppressWarnings("static-access")
	   Option view = OptionBuilder
			    .withLongOpt("view")
	   			.withArgName("Table")
	   			.hasArg()
	   			.withArgName("Column")
	   			.hasArg()
	   			.withDescription("Returns a non-editable column from a table.")
	   			.create("view");
	   
	   return view;
   }
}
