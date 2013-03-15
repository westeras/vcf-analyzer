import java.io.*;
import java.util.*;
import java.sql.*;

public class vcfWriter
{
    private BufferedWriter writer;
    
    public vcfWriter( String filename ) throws IOException
    {
        try
        {
            File outFile = new File(filename);
            this.writer = new BufferedWriter(new FileWriter(outFile));
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
    
    public void writeEntryFromDB( ResultSet entryData, 
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
            //TODO info data
            this.writer.write( entryData.getString("Format") );
            this.writer.write( '\t' );
        
        } catch (IOException exception) {
            throw new IOException("Error writing file");
        } catch (SQLException exception) {
            throw new SQLException("VCF Entry data improperly formatted");
        }
    }
    
    public void stopWriting()
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
