import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public abstract class Command {
	public abstract String execute();
	public abstract void pipeOutput();


	protected String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	
}
