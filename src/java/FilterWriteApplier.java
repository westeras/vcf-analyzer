import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sql.*;

public class FilterWriteApplier extends FilterApplier
{


    public FilterWriteApplier( String filterName, String vcfName, String filename )
    {
		this.filterName = filterName;
		this.vcfName = vcfName;
		this.fileName = filename;
    }


    protected String getSuccessMessage()
    {
    	return "Applied filter. See "+ this.fileName;
    }
}