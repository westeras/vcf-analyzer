import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;


public class CommandLineTests {

	@Test
	public void testUploadDivergenceWorks() throws IOException, ClassNotFoundException, SQLException {
		String[] commands = new String[3];
		commands[0] = "-updiv";
		commands[1] = "src/divergence.txt";
		commands[2] = "example";
		assertTrue(CommandLineInterpreter.useGnuParser(commands).equals("example1"));
	}

	@Test
	public void testUploadAnnotationWorks() throws IOException, ClassNotFoundException, SQLException {
		String[] commands = new String[2];
		commands[0] = "-upano";
		commands[1] = "src/annotation.txt";
		commands[2] = "example";
		assertTrue(CommandLineInterpreter.useGnuParser(commands).equals("example1"));
	}
	
	@Test
	public void testUploadDivergenceWorksDate() throws IOException, ClassNotFoundException, SQLException {
		String[] commands = new String[2];
		commands[0] = "-updiv";
		commands[1] = "src/divergence.txt";
		assertTrue(CommandLineInterpreter.useGnuParser(commands).equals("1-3-26-13"));
	}
	
	@Test
	public void testUploadAnnotationWorksDate() throws IOException, ClassNotFoundException, SQLException {
		String[] commands = new String[2];
		commands[0] = "-upano";
		commands[1] = "src/annotation.txt";
		assertTrue(CommandLineInterpreter.useGnuParser(commands).equals("1-3-26-13"));
	}
	
}