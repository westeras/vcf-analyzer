import org.python.core.*;
import org.python.util.PythonInterpreter;
import java.io.FileNotFoundException;

class PythonHandler {
	PythonInterpreter interpreter;
	
	public PythonHandler() {
		interpreter = new PythonInterpreter();
	}
	
	public void invokeParser(String vcfName) {
		interpreter.exec("import sys");
		interpreter.exec("print 'From Python'");
	}
	
}