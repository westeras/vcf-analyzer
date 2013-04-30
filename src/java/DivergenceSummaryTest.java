import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;



public class DivergenceSummaryTest {
	
		
	@Test
	public void testSQL() throws ClassNotFoundException, SQLException {
		DivergenceSummary summary=new DivergenceSummary("2013-03-23_15:28","2013-04-24_14-19","");
		assertEquals(summary.buildSQLStatment(),"Select `DivValue` from `Vcf`, `VcfEntry`, `Divergence`  " +
				"where `Vcf`.`VcfName`='2013-03-23_15:28' and `Vcf`.`VcfId`=`VcfEntry`.`VcfId` and `VcfEntry`.`Chrom`= `Divergence`.`Chromosome` and `VcfEntry`.`Pos`= `Divergence`.`Position` and `Divergence`.`DivName`='2013-04-24_14-19'");
	}
	
	@Test
	public void testSQL2() throws ClassNotFoundException, SQLException {
		DivergenceSummary summary=new DivergenceSummary("2013-03-23_15:28","2013-04-24_14-19","testFilter");
		assertEquals(summary.buildSQLStatment(),"Select `DivValue` from `Vcf`, `VcfEntry`, `Divergence`, `Filter`, `FilterEntryPass` " +
				"where `Vcf`.`VcfName`='2013-03-23_15:28' and `Vcf`.`VcfId`=`VcfEntry`.`VcfId` and `VcfEntry`.`Chrom`= `Divergence`.`Chromosome` and `VcfEntry`.`Pos`= `Divergence`.`Position` and `Divergence`.`DivName`='2013-04-24_14-19' " +
				"and `Filter`.`FilName`='testFilter' and `Filter`.`FilId`=`FilterEntryPass`.`FilId` and `FilterEntryPass`.`Pass`=1 and `VcfEntry`.`EntryId`=`FilterEntryPass`.`EntryId`");
	
	}
	
	@Test
	public void testSummary() throws ClassNotFoundException, SQLException{
		DivergenceSummary summary=new DivergenceSummary("2013-03-23_15:28","2013-04-24_14-19","testFilter");
		assertEquals(summary.printSummary(),"0s: 0\n1s: 0\n");
	}
	
	@Test
	public void testExecute() throws ClassNotFoundException, SQLException{
		DivergenceSummary summary=new DivergenceSummary("2013-03-23_15:28","2013-04-24_14-19","testFilter");
		DatabaseConnector conn=new DatabaseConnector();
		String sql= summary.buildSQLStatment();
		ResultSet tuples=conn.executeQuery(sql);
		summary.count(tuples);
		String test=summary.printSummary();
		assertEquals(test,"0s: 0\n1s: 0\n");
	}
	
	
	

}
