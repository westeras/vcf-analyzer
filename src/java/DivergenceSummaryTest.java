import static org.junit.Assert.*;

import org.junit.Test;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Apr 23, 2013.
 */
public class DivergenceSummaryTest {
	DivergenceSummary summary=new DivergenceSummary("2103-03-23_15:28","div_name","filter");
		
	@Test
	public void test() {
		System.out.println(this.summary.buildSQLStatment());
		fail("not yet implemented");
	}

}
