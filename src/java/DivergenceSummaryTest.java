import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Test;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Apr 23, 2013.
 */
public class DivergenceSummaryTest {
	
		
	@Test
	public void test() throws ClassNotFoundException, SQLException {
		DivergenceSummary summary=new DivergenceSummary("2103-03-23_15:28","div_name","filter");
		System.out.println(summary.buildSQLStatment());
		fail("not yet implemented");
	}

}
