
public class FilterStoreApplier extends FilterApplier
{

    public FilterStoreApplier( String filterName, String vcfName )
    {
		this.filterName = filterName;
		this.vcfName = vcfName;
    }

    protected String getSuccessMessage()
    {
    	return "Applied filter. Results are stored";
    }    
}