import org.python.core.*;
import org.python.util.PythonInterpreter;

class PythonHandler {
	PythonInterpreter interpreter;
	
	public PythonHandler() {
		interpreter = new PythonInterpreter();
	}
	
	public void invokeParser(String vcfName) {
		interpreter.exec("import sys");
		interpreter.execfile("/home/git-vcf/src/VcfParser/loadvcf");
	}
	
}