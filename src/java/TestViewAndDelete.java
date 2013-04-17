import java.util.Scanner;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Apr 16, 2013.
 */
public abstract class TestViewAndDelete {

	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner input=new Scanner(System.in);
		System.out.println("view takes a table name and column name\n please enter the table name");
		String table=input.next();
		System.out.println("enter the column name");
		String column=input.next();
		View view=new View(table, column);
		view.execute();
		
		System.out.println("delete takes a table name, column name, and a delete value\n please enter the column name");
		column=input.next();
		System.out.println("enter the delete value");
		String deleteValue=input.next();
		DeleteCommand delete=new DeleteCommand(table,column,deleteValue);
		delete.execute();
		input.close();
	}

}
