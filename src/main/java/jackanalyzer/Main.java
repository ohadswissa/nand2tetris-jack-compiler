package jackanalyzer;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        // Path to the input Jack file
        File inputFile = new File("Square/Square.jack");

        // Path to the output XML file
        File outputFile = new File("output/Square.xml");

        // Create the tokenizer and compilation engine
        JackTokenizer tokenizer = new JackTokenizer(inputFile);
        //CompilationEngine engine = new CompilationEngine(tokenizer, outputFile);

        // Compile the class and close the engine
        //engine.compileClass();
        //engine.close();

        System.out.println("Compilation finished! Output written to: " + outputFile.getAbsolutePath());
    }
}
