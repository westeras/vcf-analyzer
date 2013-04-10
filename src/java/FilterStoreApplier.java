import java.sql.*;

public class FilterStoreApplier extends FilterApplier
{
	

	public FilterStoreApplier(String vcfName, String filterName) {
		super(vcfName, filterName);
	}

	protected String getSuccessMessage()
	{
		return String.format("Filtering complete. Results are stored");
	}
	
	protected void initializeVcf( long vcfId ) throws Exception
	{
	
	}
	
	protected void processUntestedEntry( ResultSet entries ) throws Exception
	{

	}
	
	protected void processUntestedEntryInfo( String tableName, ResultSet entryInfoData ) throws Exception
	{

	}
	
	protected void processPassingEntry( ResultSet entries ) throws Exception
	{

	}
	
	protected void processUntestedIndividual() throws Exception
	{

	}
	
	protected void processUntestedIndividualData( String genotypeName, ResultSet genotypeData ) throws Exception
	{
		
	}
	
	protected void processPassingIndividual() throws Exception
	{

	}
	
	protected void finializeEntry() throws Exception
	{
	}
	
	protected void closeFiltering()
	{
	}
}