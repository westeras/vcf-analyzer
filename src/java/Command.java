import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * TODO Put here a description of what this class does.
 *
 * @author schepedw.
 *         Created Mar 14, 2013.
 */
public abstract class Command {
	public abstract String execute();
	public abstract void pipeOutput();
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @return
	 */
	protected String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	
}
