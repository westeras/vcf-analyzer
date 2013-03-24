package commandLine;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;


public class CommandLineTests {

	@Test
	public void testUploadVcfWorks() throws IOException {
		String[] commands = new String[2];
		commands[0] = "-upload";
		commands[1] = "src/test.vcf"
		assertTrue(CommandLineInterpreter.useGnuParser(commands));
	}

	@Test
	public void testUploadAnnotationWorks() throws IOException {
		String[] commands = new String[2];
		commands[0] = "-upload";
		commands[1] = "src/annotation.txt"
		assertTrue(CommandLineInterpreter.useGnuParser(commands));
	}
	
	@Test
	public void testUploadDivergenceWorks() throws IOException {
		String[] commands = new String[2];
		commands[0] = "-upload";
		commands[1] = "src/divergence.txt"
		assertTrue(CommandLineInterpreter.useGnuParser(commands));
	}
	
	@Test
	public void testFilterWorks() throws IOException {
		String[] commands = new String[6];
		commands[0] = "-createFilter";
		commands[1] = "src/test.vcf"
		assertTrue(CommandLineInterpreter.useGnuParser(commands));
	}
	
}