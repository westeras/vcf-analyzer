import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sql.*;

public abstract class FilterApplier extends Command
{

    // ########### A distinct lack of coding standards as far as tabs/spaces
    private String output = "Incomplete";
    protected String filterName;
    protected String vcfName;
    private String fileName;
	private DatabaseConnector connection;   
	private DatabaseConnector nestedConnection;
	private DatabaseConnector nestedConnection2;
    
    // ########### Why is the override annotation commented out?
    //@Override
    public String execute() {
    	
    	try {
    		this.connection = new DatabaseConnector();
    		this.nestedConnection = new DatabaseConnector();
    		this.nestedConnection2 = new DatabaseConnector();
    	}
    	// ########### doing this Pokemon exception handling is usually a sign that maybe
    	// it isn't this class's responsibility to handle this exception. Should probably reconsider
    	catch( Exception e)
    	{
    		this.output = "Cannot connect to database";
    		return this.output;
    	}
	
		try
		    {

			//TODO load filter
			this.output = applyFilter();
			connection.CloseConnection();
			nestedConnection.CloseConnection();
			nestedConnection2.CloseConnection();
		    }
		catch (Exception e)
		    {
			this.output = e.getMessage();
		    }
		return this.output;
	
    }

    private String applyFilter()
    {
    	VcfWriter writer = null;
		try {
			boolean passing = true;
			writer = new VcfWriter(this.fileName);
			
			long vcfId = this.connection.getVcfId( this.vcfName);
			
			writer.writeHeader( this.connection.getVcfHeader(vcfId) );
			
			ResultSet entries = this.connection.getVcfEntries( vcfId );
			
			// ########### Things get a little complex here. I think I can see some
			// discrete steps (I may be wrong). If so, then these can be extracted 
			// into separate methods
			while (entries.next() )
		    {
				long entryId = entries.getLong("EntryId");
				ArrayList<String> infoTableName = new ArrayList<String>();

	    		writer.writeEntryStart( entries );
	    		
	    		ArrayList<String> tableNames = this.nestedConnection.getInfoTableNames();
	    		for (int j=0; j< tableNames.size(); j++)
	    		{
	    			ResultSet entryInfoData = this.nestedConnection.getInfoDatum(entryId, tableNames.get(j));
	    			if (entryInfoData!=null)
	    			{
	    				//writes each info datum; eg 
	    				writer.writeInfoSection( tableNames.get(j), entryInfoData );
	    			}
	    		}
	    		writer.writeEntryEnd(entries);
	    		
				//TODO test entry
	    		
				if (passing)
				{
					//individual level
					String indFormat = entries.getString("Format");
					ArrayList<String> genotypes = new ArrayList<String>( 
				    		Arrays.asList( indFormat.split(":") ));
					
					ResultSet individuals = this.nestedConnection.getIndividuals( entryId );
					while (individuals.next() )
					{
						writer.writeIndividualStart();
						
						long indId = individuals.getLong("IndID");
						for (int k=0; k< genotypes.size(); k++)
						{
							ResultSet genotypeData = this.nestedConnection2.getIndividualDatum( indId, genotypes.get(k) );
							
							writer.writeIndividualDatum( genotypeData, genotypes.get(k));
							genotypeData.close();
						}
						
						writer.writeIndividualEnd();
						//if pass
						//if pass write
						//if fail close genotypeData
						//writer closes genotypeData,
						//writer.writeIndividual( genotypeData, genotypes );
					}
					
					individuals.close();
					writer.writeEOL();
				}

		    }
			entries.close();
			writer.closeWriter();
			return getSuccessMessage();
			
		} catch (Exception exception) {
			if (writer != null)
			{
				writer.closeWriter();
			}
			return exception.getMessage();
		}
    }

    //@Override
    public void pipeOutput() {
	// TODO Auto-generated method stub.
	
    }
    
    abstract protected String getSuccessMessage();

}
