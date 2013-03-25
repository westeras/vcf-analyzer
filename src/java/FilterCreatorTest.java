import java.sql.SQLException;


public class FilterCreatorTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		String filterName = "testFilter";
		String[] operands = {"AC > 1", "AF = 0", "QD between 10 15"};
		FilterCreator testCreator = new FilterCreator(filterName, operands);
		testCreator.uploadEntries();
	}

}
