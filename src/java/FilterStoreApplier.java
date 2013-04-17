import java.sql.*;

public class FilterStoreApplier extends FilterApplier
{
	private long currentEntryId;
	private long currentIndId;

	public FilterStoreApplier(String vcfName, String filterName) {
		super(vcfName, filterName);
	}

	protected String getSuccessMessage()
	{
		return String.format("Filtering complete. Results are stored");
	}
	
	protected void initializeVcf( long vcfId ) throws Exception
	{
		//do nothing
	}
	
	protected void processUntestedEntry( ResultSet entries ) throws Exception
	{
		this.currentEntryId = entries.getLong("EntryId");
	}
	
	protected void processUntestedEntryInfo( String tableName, ResultSet entryInfoData ) throws Exception
	{

	}
	
	protected void processPassingEntry( ResultSet entries ) throws Exception
	{

	}
	
	protected void processUntestedIndividual( long indId) throws Exception
	{
		currentIndId = indId;
	}
	
	protected void processUntestedIndividualData( String genotypeName, ResultSet genotypeData ) throws Exception
	{
		
	}
	
	protected void processPassingIndividual() throws Exception
	{
		if (this.filterId > 0)
		{
			this.nestedConnection3.insertIndividualPass(this.filterId, this.currentIndId, 1);
		}
	}
	
	protected void processFailingIndividual() throws Exception
	{
		//do nothing
	}
	
	protected void finializeEntry() throws Exception
	{
		//entry has fully passed on entry and ind levels
		//1 indicates a complete pass
		//2 indicates the entry passed but some individuals may not have
		if (this.filterId > 0)
		{
			int passing = 1;
			if ( this.individualParameters.size() > 0 )
			{
				passing = 2;
			}
			this.nestedConnection.insertEntryPass( this.filterId, this.currentEntryId, passing );
		}
		
	}
	
	protected void finializeEntryFailing() throws Exception
	{
		//entry failed
		if (this.filterId > 0)
		{
			this.nestedConnection.insertEntryPass( this.filterId, this.currentEntryId, 0);
		}
	}
	
	protected void closeFiltering()
	{
	}
}