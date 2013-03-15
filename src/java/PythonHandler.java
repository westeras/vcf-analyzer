import org.python.core.*;
import org.python.util.PythonInterpreter;
import java.io.File;

class PythonHandler {
	PythonInterpreter interpreter;
	
	public PythonHandler() {
		interpreter = new PythonInterpreter();
	}
	
	public void invokeParser(String vcfFilePath) {
		if (!fileExists(vcfFilePath)) {
			System.out.println("Fatal Error: file does not exist");
			return;
		}
		//interpreter.exec("import sys");
		//interpreter.exec("import os");
		
		//interpreter.exec("os.chdir('../VcfParser/')");
		//interpreter.exec("os.listdir('.')");
		System.out.println("VCF upload complete");
	}
	
	private boolean fileExists(String filePath) {
		File f = new File(filePath);
		return f.exists();
	}
	
}
