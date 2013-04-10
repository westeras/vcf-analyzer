import java.sql.*;

public class FilterApplierWriter extends FilterApplier{

	protected String fileName;
	private VcfWriter writer;
	
	public FilterApplierWriter(String vcfName, String filterName, String fileName)
	{
		super(vcfName, filterName);
		this.fileName = fileName;
		this.writer = null;
	}	
	
	public FilterApplierWriter(String vcfName, String filterName )
	{
		super(vcfName, filterName);
		this.fileName = filterName + "-" + getDate();
		this.writer = null;
	}		
	
	protected String getSuccessMessage()
	{
		return String.format("Filtering complete see %s", this.fileName);
	}
	
	protected void initializeVcf( long vcfId ) throws Exception
	{
		writer = new VcfWriter(this.fileName);
		writer.writeHeader( this.connection.getVcfHeader(vcfId) );
	}
	
	protected void processUntestedEntry( ResultSet entries ) throws Exception
	{
		writer.writeEntryStart( entries );
	}
	
	protected void processUntestedEntryInfo( String tableName, ResultSet entryInfoData ) throws Exception
	{
		writer.writeInfoSection( tableName, entryInfoData );
	}
	
	protected void processPassingEntry( ResultSet entries ) throws Exception
	{
		writer.writeEntryEnd(entries);
	}
	
	protected void processUntestedIndividual() throws Exception
	{
		writer.writeIndividualStart();
	}
	
	protected void processUntestedIndividualData( String genotypeName, ResultSet genotypeData ) throws Exception
	{
		writer.writeIndividualDatum( genotypeData, genotypeName);
		
	}
	
	protected void processPassingIndividual() throws Exception
	{
		writer.writeIndividualEnd();
	}
	
	protected void finializeEntry() throws Exception
	{
		writer.writeEOL();
	}
	
	protected void closeFiltering()
	{
		if (writer != null)
		{
			writer.closeWriter();
		}
	}
}
