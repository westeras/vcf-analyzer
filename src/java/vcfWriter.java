import java.io.*;
import java.util.*;
import java.sql.*;

public class vcfWriter
{
    private BufferedWriter writer;
    private int infoCount = 0;
    
    public vcfWriter( String filename ) throws IOException
    {
        try
        {
            File outFile = new File(filename);
            this.writer = new BufferedWriter(new FileWriter(outFile));
            System.out.println("writer created");
        }
        catch (IOException e)
        {
            throw new IOException("Could not open file:" + filename);
        }
    }
    
    public void writeHeader(String header) throws IOException
    {
        this.writer.write( header);
        this.writer.newLine();
    }
    
    public void writeEntry( ResultSet entryData, 
                                    ArrayList<ResultSet> infoData,
                                    ArrayList<String> infoName ) throws IOException, SQLException
    {
        
        try {
            this.writer.write( entryData.getString("Chrom") );
            this.writer.write( '\t' );
            this.writer.write( entryData.getString("Pos") );
            this.writer.write( '\t' );
            this.writer.write( entryData.getString("Id") );
            this.writer.write( '\t' );
            this.writer.write( entryData.getString("Ref") );
            this.writer.write( '\t' );
            this.writer.write( entryData.getString("Alt") );
            this.writer.write( '\t' );
            this.writer.write( entryData.getString("Qual") );
            this.writer.write( '\t' );
            this.writer.write( entryData.getString("Filter") );
            this.writer.write( '\t' );

            this.infoCount = 0;
    	    for (int i =0; i< infoData.size(); i++ )
    		{
    		    if (i!= 0)
    		    {
    		    	this.writer.write(";");
    		    }
    		    
    		    ResultSet rs = infoData.get(i);
    		    String infoDatum = "";
    		    if (rs.next()) 
    			{
    			    ResultSetMetaData rsMetaData = rs.getMetaData();

    			    int numberOfColumns = rsMetaData.getColumnCount();
    		    	
    			    if (numberOfColumns > 1)
    			    {
    			    	infoDatum = rs.getNString(2);
    			    	this.writer.write( infoName.get(i)+"="+infoDatum );
    			    }
    			    else
    			    {
    			    	this.writer.write( infoName.get(i) );
    			    }
    			}
    		    rs.close();

    		}
            
            this.writer.write( entryData.getString("Format") );
        
        } catch (IOException exception) {
            throw new IOException("Error writing file");
        } catch (SQLException exception) {
        	throw exception;
        	//TODO change back
            //throw new SQLException("VCF Entry data improperly formatted");
        }
    }
    
    public void writeEntryEnd( ResultSet entryData ) throws IOException, SQLException
    {
    	this.writer.write("/t");
    	this.writer.write( entryData.getString("Format") );
    }
    
    public void writeInfoSection( String infoName, ResultSet infoData) throws IOException, SQLException
    {
	    if (this.infoCount!= 0)
	    {
	    	this.writer.write(";");
	    }
	    
	    String infoDatum = "";
	    if (infoData.next()) 
		{
		    ResultSetMetaData rsMetaData = infoData.getMetaData();

		    int numberOfColumns = rsMetaData.getColumnCount();
	    	
		    if (numberOfColumns > 1)
		    {
		    	infoDatum = infoData.getNString(2);
		    	this.writer.write( infoName+"="+infoDatum );
		    }
		    else
		    {
		    	this.writer.write( infoName );
		    }
		}
	    infoData.close();
    }
    
    public void writeIndividual( 
            ArrayList<ResultSet> genotypeData,
            ArrayList<String> genotypeName ) throws IOException, SQLException
	{
    	this.writer.write("\t");
    	if ( genotypeData == null)
    	{
    		return;
    	}
    	
    	for (int i=0; i < genotypeName.size(); i++)
    	{
    		if ( i!= 0)
    		{
    			this.writer.write(":");
    		}
    		if ( isSpecialCase(genotypeName.get(i) ) )
    		{
    				
    			this.writer.write( formatSpecialCase( genotypeName.get(i), genotypeData.get(i) ) );
    		}
    		else
    		{
    			this.writer.write( formatStandardCase( genotypeName.get(i), genotypeData.get(i) ) );
    		}	
    		
    		genotypeData.get(i).close();
    	} 	
    	
	}
    
	private String formatStandardCase(String genotypeName, ResultSet rs) throws SQLException {

	    ResultSetMetaData rsMetaData = rs.getMetaData();

	    int numberOfColumns = rsMetaData.getColumnCount();
	    
	    String indData = "";
	    String nullValues = null;
	    
	    for (int i=2; i<= numberOfColumns; i++)
	    {
	    	String separator = ",";
	    	if (i==2)
	    	{
	    		separator = "";
	    	}
	    	
	    	String genoDatum = rs.getNString(i);
	    	if ( rs.wasNull() )
	    	{
	    		if ( nullValues == null )
	    			nullValues = separator + ".";
	    		else
	    			nullValues += separator + ".";
	    	}
	    	else
	    	{
	    		if ( nullValues == null )
	    		{
	    			//valid value; append stored nulls
	    			indData += nullValues;
	    			nullValues = null;
	    		}
	    		indData += separator + genoDatum;
	    	}
	    	
	    }
	    if (indData.isEmpty())
	    {
	    	indData = ".";
	    }
	    
		return indData;
	}

	private boolean isSpecialCase(String genotypeName ) throws SQLException
	{
		return genotypeName.equals( "GT");
	}
	
	private String formatSpecialCase(String genotypeName, ResultSet data) throws IOException, SQLException {
		if ( genotypeName.equals( "GT") )
		{
			int i = 0;
			String gtData = "";
			if (data.next() )
			{
				appendAllele( gtData, data, "1") ;
				if ( appendPhase( gtData, data, "1") )
				{
					appendAllele( gtData, data, "2") ;
					if (appendPhase( gtData, data, "2") )
					{
						appendAllele( gtData, data, "3");
					}
				}
				return gtData;
			}
			else
			{
				return ".";
			}
			
		}
		return "";
	}
	
	private void appendAllele( String gtData, ResultSet data, String count )throws SQLException
	{
		String allele = data.getString("Allele" +count);
		if ( data.wasNull() )
		{
			gtData += ".";
		}
		else
		{
			gtData += allele;
		}
		
	}
	
	private boolean appendPhase( String gtData, ResultSet data, String count )throws SQLException
	{
		byte phase = data.getByte("Phase" +count);
		if ( data.wasNull() )
		{
			//end of data
			return false;
		}
		else
		{
			if ( phase == 0 )
			{
				gtData = "/";
			}
			else if ( phase == 1 )
			{
				gtData = "|";
			}
			else
			{
				throw new SQLException("Invalid GT data");
			}
			return true;
		}
	}

	public void writeEOL() throws IOException
    {
    	this.writer.write("\n");
    	
    }
    
    public void closeWriter()
    {
        if ( this.writer != null )
        {
            try {
                this.writer.close();
            } catch (IOException exception) {
                //Do nothing
            }
        }
    }
}
