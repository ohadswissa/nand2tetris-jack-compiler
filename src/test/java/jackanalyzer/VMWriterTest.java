package jackanalyzer;

import java.io.File;
import java.io.IOException;

public class VMWriterTest {
    public static void main(String[] args) {
        try {
            // Create an instance of VMWriter to write to the test.vm file
            File outputFile = new File("test.vm");
            System.out.println("Attempting to write to: " + outputFile.getAbsolutePath());

            VMWriter writer = new VMWriter(outputFile);

            // Test push and pop with correct enums
            writer.writePush(VMWriter.Segment.CONST, 10);  // Use SEGMENT.CONST instead of "constant"
            writer.writePop(VMWriter.Segment.LOCAL, 0);    // Use SEGMENT.LOCAL instead of "Local"

            // Test arithmetic with correct enum
            writer.writeArithmetic(VMWriter.Operation.ADD);  // Use OPERATION.ADD instead of "add"

            // Test labels and flow control
            writer.writeLabel("LOOP_START");
            writer.writeGoto("LOOP_START");
            writer.writeIf("END");

            // Test function calls
            writer.writeFunction("Main.main", 2);  // Correct function name and argument count
            writer.writeCall("Math.multiply", 2);  // Correct function name and argument count
            writer.writeReturn();

            // Close the writer
            writer.close();

            System.out.println("VM file successfully written!");

        } catch (IOException e) {
            System.out.println("IOException caught: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
