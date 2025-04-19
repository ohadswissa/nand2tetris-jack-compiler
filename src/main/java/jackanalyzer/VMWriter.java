package jackanalyzer;

import java.util.*;
import java.io.*;

/**
 * VMWriter is responsible for generating VM code that can be executed by the VM.
 * It writes commands related to memory management (push/pop), arithmetic operations,
 * and control flow (labels, jumps, calls, returns).
 *
 * Key Methods:
 * - writePush: Writes a VM push command.
 * - writePop: Writes a VM pop command.
 * - writeArithmetic: Writes a VM arithmetic command.
 * - writeLabel: Writes a VM label command.
 * - writeGoTo: Writes a VM goto command.
 * - writeIf: Writes a VM if-goto command.
 * - writeCall: Writes a VM call command.
 * - writeFunction: Writes a VM function declaration command.
 * - writeReturn: Writes a VM return command.
 */
public class VMWriter {

    // Enum representing the different segments in the VM memory model
    public static enum Segment {
        CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP, NONE
    }

    // Enum representing the different arithmetic operations in the VM
    public static enum Operation {
        ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT
    }

    // Mapping segments to their string representation
    private static HashMap<Segment, String> segmentMap = new HashMap<>();

    // Mapping operations to their string representation
    private static HashMap<Operation, String> operationMap = new HashMap<>();

    // The BufferedWriter that writes the VM code to an output file
    private BufferedWriter writer;

    // Static initialization block for initializing segment and operation mappings
    static {
        // Segment mappings
        segmentMap.put(Segment.CONST, "constant");
        segmentMap.put(Segment.ARG, "argument");
        segmentMap.put(Segment.LOCAL, "local");
        segmentMap.put(Segment.NONE, "none"); // This may be deleted later if not needed
        segmentMap.put(Segment.POINTER, "pointer");
        segmentMap.put(Segment.STATIC, "static");
        segmentMap.put(Segment.TEMP, "temp");
        segmentMap.put(Segment.THAT, "that");
        segmentMap.put(Segment.THIS, "this");

        // Operation mappings
        operationMap.put(Operation.ADD, "add");
        operationMap.put(Operation.SUB, "sub");
        operationMap.put(Operation.NEG, "neg");
        operationMap.put(Operation.EQ, "eq");
        operationMap.put(Operation.GT, "gt");
        operationMap.put(Operation.LT, "lt");
        operationMap.put(Operation.AND, "and");
        operationMap.put(Operation.OR, "or");
        operationMap.put(Operation.NOT, "not");
    }

    /**
     * Constructor initializes the BufferedWriter to write VM code to the provided output file.
     *
     * @param outputFile The file to write the VM code to.
     * @throws IOException if there is an error during file handling.
     */
    public VMWriter(File outputFile) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFile));
    }

    /**
     * Writes a VM push command.
     *
     * @param segment The segment from which to push (e.g., constant, argument).
     * @param index The index of the variable in the segment.
     * @throws IOException if there is an error writing the command.
     */
    public void writePush(Segment segment, int index) throws IOException {
        writeCommand("push", segmentMap.get(segment), String.valueOf(index));
    }

    /**
     * Writes a VM pop command.
     *
     * @param segment The segment into which to pop (e.g., local, that).
     * @param index The index of the variable in the segment.
     * @throws IOException if there is an error writing the command.
     */
    public void writePop(Segment segment, int index) throws IOException {
        writeCommand("pop", segmentMap.get(segment), String.valueOf(index));
    }

    /**
     * Writes a VM arithmetic operation command (e.g., add, sub, neg).
     *
     * @param operation The arithmetic operation to perform (e.g., add, sub).
     * @throws IOException if there is an error writing the command.
     */
    public void writeArithmetic(Operation operation) throws IOException {
        writeCommand(operationMap.get(operation), "", "");
    }

    /**
     * Writes a VM label command, which is used to mark a location in the code.
     *
     * @param label The label to write.
     * @throws IOException if there is an error writing the command.
     */
    public void writeLabel(String label) throws IOException {
        writeCommand("label", label, "");
    }

    /**
     * Writes a VM goto command, which directs control to the specified label.
     *
     * @param label The label to go to.
     * @throws IOException if there is an error writing the command.
     */
    public void writeGoto(String label) throws IOException {
        writeCommand("goto", label, "");
    }

    /**
     * Writes a VM if-goto command, which branches to the specified label if the top of the stack is true.
     *
     * @param label The label to go to if the condition is true.
     * @throws IOException if there is an error writing the command.
     */
    public void writeIf(String label) throws IOException {
        writeCommand("if-goto", label, "");
    }

    /**
     * Writes a VM call command, which calls a subroutine with a given number of arguments.
     *
     * @param name The name of the subroutine to call.
     * @param arguments The number of arguments to pass to the subroutine.
     * @throws IOException if there is an error writing the command.
     */
    public void writeCall(String name, int arguments) throws IOException {
        writeCommand("call", name, String.valueOf(arguments));
    }

    /**
     * Writes a VM function declaration command.
     *
     * @param name The name of the function.
     * @param localVars The number of local variables the function uses.
     * @throws IOException if there is an error writing the command.
     */
    public void writeFunction(String name, int localVars) throws IOException {
        writeCommand("function", name, String.valueOf(localVars));
    }

    /**
     * Writes a VM return command, which indicates the end of a subroutine.
     *
     * @throws IOException if there is an error writing the command.
     */
    public void writeReturn() throws IOException {
        writeCommand("return", "", "");
    }

    /**
     * A helper method to write the actual command to the output file.
     *
     * @param command The VM command (e.g., "push", "pop").
     * @param param1 The first parameter for the command (e.g., segment name).
     * @param param2 The second parameter for the command (e.g., index or label).
     * @throws IOException if there is an error writing the command.
     */
    void writeCommand(String command, String param1, String param2) throws IOException {
        writer.write(command + " " + param1 + " " + param2 + "\n");
    }

    /**
     * Closes the BufferedWriter, finalizing the writing process.
     *
     * @throws IOException if there is an error closing the writer.
     */
    public void close() throws IOException {
        writer.close();
    }
}
