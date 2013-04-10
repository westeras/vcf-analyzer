import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sql.*;

public abstract class FilterApplier extends Command
{

	private String output = "Incomplete";
	protected String filterName;
	protected String vcfName;
	protected String fileName;
	protected DatabaseConnector connection;	
	protected DatabaseConnector nestedConnection;
	protected DatabaseConnector nestedConnection2;
	private ArrayList<FilterParameter> entryParameters;
	private ArrayList<FilterParameter> individualParameters;
	
	public FilterApplier(String vcfName, String filterName)
	{
		this.vcfName = vcfName;
		this.filterName = filterName;		
	}
	
	//Template methods
	protected abstract String getSuccessMessage();
	protected abstract void initializeVcf( long vcfId ) throws Exception;
	protected abstract void processUntestedEntry( ResultSet entries ) throws Exception;
	protected abstract void processUntestedEntryInfo( 
			String tableName, ResultSet entryInfoData ) throws Exception;
	protected abstract void processPassingEntry( ResultSet entries ) throws Exception;
	protected abstract void processUntestedIndividual() throws Exception;
	protected abstract void processUntestedIndividualData( String genotypeName, ResultSet genotypeData ) throws Exception;
	protected abstract void processPassingIndividual() throws Exception;
	protected abstract void finializeEntry() throws Exception;
	protected abstract void closeFiltering();
	
	@Override
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
			loadFilter();
			this.output = applyFilter();
			connection.CloseConnection();
			nestedConnection.CloseConnection();
			nestedConnection2.CloseConnection();
			}
		catch (Exception e)
			{
			connection.CloseConnection();
			nestedConnection.CloseConnection();
			nestedConnection2.CloseConnection();
			this.output = e.getMessage();
			}
		return this.output;
	
	}

	private void loadFilter() throws Exception
	{
		int filterId = this.connection.getFilterID(this.filterName);
		this.connection.getFilterEntries(filterId);
		
		this.connection.getFilterIndividuals(filterId);
		
		//entryParameters = connection.;
		//individualParameters;
		
	}
	
	private String applyFilter()
	{
		try {
			boolean passing = true;
			
			long vcfId = this.connection.getVcfId( this.vcfName);
			
			initializeVcf( vcfId );
			
			ResultSet entries = this.connection.getVcfEntries( vcfId );
			
			// ########### Things get a little complex here. I think I can see some
			// discrete steps (I may be wrong). If so, then these can be extracted 
			// into separate methods
			while (entries.next() )
			{
				long entryId = entries.getLong("EntryId");

				processUntestedEntry( entries );
				
				ArrayList<String> tableNames = this.nestedConnection.getInfoTableNames();
				for (int j=0; j< tableNames.size(); j++)
				{
					ResultSet entryInfoData = this.nestedConnection.getInfoDatum(entryId, tableNames.get(j));
					if (entryInfoData!=null)
					{
						//writes each info datum; eg 
						processUntestedEntryInfo( tableNames.get(j), entryInfoData );
					}
				}

				
				//TODO test entry
				
				if (passing)
				{
					processPassingEntry(entries);
					
					//individual level
					String indFormat = entries.getString("Format");
					ArrayList<String> genotypes = new ArrayList<String>( 
							Arrays.asList( indFormat.split(":") ));
					
					ResultSet individuals = this.nestedConnection.getIndividuals( entryId );
					while (individuals.next() )
					{
						processUntestedIndividual();
						
						long indId = individuals.getLong("IndID");
						for (int k=0; k< genotypes.size(); k++)
						{
							ResultSet genotypeData = this.nestedConnection2.getIndividualDatum( indId, genotypes.get(k) );
							
							processUntestedIndividualData( genotypes.get(k), genotypeData);
							genotypeData.close();
						}
						
						if (passing)
						{
							processPassingIndividual();
						}
					}
					
					individuals.close();
					finializeEntry();
				}

			}
			entries.close();
			closeFiltering();
			return getSuccessMessage();
			
		} catch (Exception exception) {
			closeFiltering();
			return exception.getMessage();
		}
	}

	//@Override
	public void pipeOutput() {
	// TODO Auto-generated method stub.
	
	}

}
