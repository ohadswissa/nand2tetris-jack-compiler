package jackanalyzer;
import java.io.*;

/**
 * CompilationEngine is responsible for compiling a complete Jack class,
 * including variable declarations and subroutine declarations.
 *
 * @throws IOException If there is an issue during tokenization or VM writing.
 */
public class CompilationEngine {
    private VMWriter vmwriter;
    private JackTokenizer tokenizer; // as implemented in JackTokenizer class.
    private SymbolTable table; // as implemented in SymbolTable class.
    private String CurrentclassName;
    private String CurrentSubroutineName;
    private int CurrentIndexLabel; // Index for each label

    /**
     * Throws an exception to report errors during tokenization.
     *
     * @param val The expected token.
     */
    private void TokenDebugger(String val) {
        throw new IllegalStateException("Expected: " + val + " Actual: " + tokenizer.getCurrentToken());
    }

    /**
     * Checks that the current symbol matches the expected one.
     *
     * @param symbol The expected symbol.
     * @throws IOException if there is an error during tokenization.
     */
    private void checkSymbol(char symbol) throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenType.SYMBOL || tokenizer.symbol() != symbol) {
            TokenDebugger("'" + symbol + "'");
        }
    }

    /**
     * Constructor initializes the tokenizer, symbol table, and VMWriter.
     *
     * @param inputFile The input file containing the Jack program.
     * @param outFile   The output file where VM code will be written.
     * @throws IOException if there is an error during initialization.
     */
    public CompilationEngine(File inputFile, File outFile) throws IOException {
        tokenizer = new JackTokenizer(inputFile);
        table = new SymbolTable();
        vmwriter = new VMWriter(outFile);
        CurrentIndexLabel = 0; // No labels processed yet
    }

    /**
     * Returns the full function name including class and subroutine names.
     *
     * @return the current function in the form "className.subclassName"
     */
    public String currentfunction() {
        if (CurrentclassName.length() != 0 && CurrentSubroutineName.length() != 0) {
            return CurrentclassName + "." + CurrentSubroutineName; // "foo.bar"
        }
        return "";
    }

    /**
     * Compiles a type, returning either a primitive type (int, char, boolean)
     * or a class name.
     *
     * @return the compiled type.
     * @throws IOException if there is an error during tokenization.
     */
    public String compileType() throws IOException {
        tokenizer.advance(); // Fetch the next token
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && (tokenizer.keyWord().equals(JackTokenizer.Keyword.INT) || tokenizer.keyWord().equals(JackTokenizer.Keyword.CHAR) || tokenizer.keyWord().equals(JackTokenizer.Keyword.BOOLEAN))) {
            return tokenizer.getCurrentToken();
        }
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            return tokenizer.identifier();
        }
        return "";
    }

    /**
     * Compiles a class declaration, including class-level variable declarations.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    public void compileClass() throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenType.KEYWORD ||
                tokenizer.keyWord() != JackTokenizer.Keyword.CLASS) {
            TokenDebugger("class");
        }
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenType.IDENTIFIER) {
            TokenDebugger("className");
        }
        CurrentclassName = tokenizer.identifier();
        checkSymbol('{');
        compileClassVarDec();
        compileSubroutine();
        checkSymbol('}');
        if (tokenizer.hasMoreTokens()) {
            throw new IllegalStateException("Unexpected tokens");
        }
        vmwriter.close();
    }

    /**
     * Compiles class-level variable declarations (static or field).
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileClassVarDec() throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            tokenizer.decrementPointer();
            return;
        }

        if (tokenizer.keyWord() == JackTokenizer.Keyword.CONSTRUCTOR || tokenizer.keyWord() == JackTokenizer.Keyword.FUNCTION || tokenizer.keyWord() == JackTokenizer.Keyword.METHOD) {tokenizer.decrementPointer();
            return;
        }
        SymbolTable.Symbol.Kind kind = null;
        String type = "";
        String name = "";
        switch (tokenizer.keyWord()) {
            case STATIC:
                kind = SymbolTable.Symbol.Kind.STATIC;
                break;
            case FIELD:
                kind = SymbolTable.Symbol.Kind.FIELD;
                break;
        }
        type = compileType();
        do {
            tokenizer.advance();
            name = tokenizer.identifier();
            table.define(name, type, kind);
            tokenizer.advance();
            if (tokenizer.symbol() == ';') {
                break;
            }
        } while (true);
        compileClassVarDec();
    }


    /**
     * Compiles a complete subroutine, including the subroutine's signature
     * and body.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileSubroutine() throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            tokenizer.decrementPointer();
            return;
        }
        JackTokenizer.Keyword keyword = tokenizer.keyWord();
        table.startSubroutine();
        if (tokenizer.keyWord() == JackTokenizer.Keyword.METHOD) {
            table.define("this", CurrentclassName, SymbolTable.Symbol.Kind.ARG);
        }
        String type = "";
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyWord() == JackTokenizer.Keyword.VOID) {
            type = "void";
        } else {
            tokenizer.decrementPointer();
            type = compileType();
        }
        tokenizer.advance();
        CurrentSubroutineName = tokenizer.identifier();
        checkSymbol('(');
        compileParameterList();
        checkSymbol(')');
        compileSubroutineBody(keyword);
        compileSubroutine();
    }

    /**
     * Compiles the body of a subroutine, including variable declarations
     * and the subroutine's statements.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileSubroutineBody(JackTokenizer.Keyword keyword) throws IOException {
        checkSymbol('{');
        compileVarDec();
        writeFunctionDec(keyword);
        compileStatement();
        checkSymbol('}');
    }

    /**
     * Writes the function declaration and handles the "this" pointer
     * for methods and constructors.
     *
     * @param keyword The type of the subroutine (METHOD, CONSTRUCTOR, etc.)
     * @throws IOException if there is an error during writing.
     */
    private void writeFunctionDec(JackTokenizer.Keyword keyword) throws IOException {
        vmwriter.writeFunction(currentfunction(), table.varCount(SymbolTable.Symbol.Kind.VAR));
        if (keyword == JackTokenizer.Keyword.METHOD) {
            vmwriter.writePush(VMWriter.Segment.ARG, 0);
            vmwriter.writePop(VMWriter.Segment.POINTER, 0);
        } else if (keyword == JackTokenizer.Keyword.CONSTRUCTOR) {
            vmwriter.writePush(VMWriter.Segment.CONST, table.varCount(SymbolTable.Symbol.Kind.FIELD));
            vmwriter.writeCall("Memory.alloc", 1);
            vmwriter.writePop(VMWriter.Segment.POINTER, 0);
        }
    }

    /**
     * Compiles a single statement in the Jack language.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileStatement() throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '}') {
            tokenizer.decrementPointer();
            return;
        } else {
            switch (tokenizer.keyWord()) {
                case LET:
                    compileLet();
                    break;
                case IF:
                    compileIf();
                    break;
                case WHILE:
                    compileWhile();
                    break;
                case DO:
                    compileDo();
                    break;
                case RETURN:
                    compileReturn();
                    break;
            }
        }
        compileStatement();
    }

    /**
     * Compiles a parameter list for a subroutine.
     *
     * @throws IOException if there is an error during tokenization.
     */
    private void compileParameterList() throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.decrementPointer();
            return;
        }
        String type = "";
        tokenizer.decrementPointer();
        do {
            type = compileType();
            tokenizer.advance();
            table.define(tokenizer.identifier(), type, SymbolTable.Symbol.Kind.ARG);
            tokenizer.advance();
            if (tokenizer.symbol() == ')') {
                tokenizer.decrementPointer();
                break;
            }
        } while (true);
    }

    /**
     * Compiles a variable declaration.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileVarDec() throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenType.KEYWORD ||
                tokenizer.keyWord() != JackTokenizer.Keyword.VAR) {
            tokenizer.decrementPointer();
            return;
        }
        String type = compileType();
        do {
            tokenizer.advance();
            table.define(tokenizer.identifier(), type, SymbolTable.Symbol.Kind.VAR);
            tokenizer.advance();
            if (tokenizer.symbol() == ';') {
                break;
            }
        } while (true);
        compileVarDec();
    }

    /**
     * Compiles a do statement.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileDo() throws IOException {
        compileSubroutineCall();
        checkSymbol(';');
        vmwriter.writePop(VMWriter.Segment.TEMP, 0);
    }

    /**
     * Compiles a let statement.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileLet() throws IOException {
        tokenizer.advance();
        String varName = tokenizer.identifier();
        tokenizer.advance();
        boolean expressionExist = false;
        if (tokenizer.symbol() == '[') {
            expressionExist = true;
            vmwriter.writePush(getSeg(table.kindOf(varName)), table.indexOf(varName));
            compileExpression();
            checkSymbol(']');
            vmwriter.writeArithmetic(VMWriter.Operation.ADD);
        }
        if (expressionExist) {
            tokenizer.advance();
        }
        compileExpression();
        checkSymbol(';');
        if (expressionExist) {
            vmwriter.writePop(VMWriter.Segment.TEMP, 0);
            vmwriter.writePop(VMWriter.Segment.POINTER, 1);
            vmwriter.writePush(VMWriter.Segment.TEMP, 0);
            vmwriter.writePop(VMWriter.Segment.THAT, 0);
        } else {
            vmwriter.writePop(getSeg(table.kindOf(varName)), table.indexOf(varName));
        }
    }

    /**
     * Returns the corresponding segment for the input kind.
     *
     * @param kind The variable kind.
     * @return The corresponding segment.
     */
    private VMWriter.Segment getSeg(SymbolTable.Symbol.Kind kind) {
        switch (kind) {
            case FIELD:
                return VMWriter.Segment.THIS;
            case STATIC:
                return VMWriter.Segment.STATIC;
            case VAR:
                return VMWriter.Segment.LOCAL;
            case ARG:
                return VMWriter.Segment.ARG;
            default:
                return VMWriter.Segment.NONE;
        }
    }

    /**
     * Compiles a while statement.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileWhile() throws IOException {
        String contLabel = newLabel();
        String topLabel = newLabel();
        vmwriter.writeLabel(topLabel);
        checkSymbol('(');
        compileExpression();
        checkSymbol(')');
        vmwriter.writeArithmetic(VMWriter.Operation.NOT);
        vmwriter.writeIf(contLabel);
        checkSymbol('{');
        compileStatement();
        checkSymbol('}');
        vmwriter.writeGoto(topLabel);
        vmwriter.writeLabel(contLabel);
    }

    /**
     * Generates a new label based on the label index.
     *
     * @return The generated label.
     */
    private String newLabel() {
        return "LABEL_" + (CurrentIndexLabel++);
    }

    /**
     * Compiles a return statement.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileReturn() throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ';') {
            vmwriter.writePush(VMWriter.Segment.CONST, 0);
        } else {
            tokenizer.decrementPointer();
            compileExpression();
            checkSymbol(';');
        }
        vmwriter.writeReturn();
    }

    /**
     * Compiles an if statement with an optional else block.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileIf() throws IOException {
        String elseLabel = newLabel();
        String endLabel = newLabel();
        checkSymbol('(');
        compileExpression();
        checkSymbol(')');
        vmwriter.writeArithmetic(VMWriter.Operation.NOT);
        vmwriter.writeIf(elseLabel);
        checkSymbol('{');
        compileStatement();
        checkSymbol('}');
        vmwriter.writeGoto(endLabel);
        vmwriter.writeLabel(elseLabel);
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.keyWord() == JackTokenizer.Keyword.ELSE) {
            checkSymbol('{');
            compileStatement();
            checkSymbol('}');
        } else {
            tokenizer.decrementPointer();
        }
        vmwriter.writeLabel(endLabel);
    }

    /**
     * Compiles a term in an expression (e.g., variable, constant, subroutine call).
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileTerm() throws IOException {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            String tempId = tokenizer.identifier();
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '[') {
                vmwriter.writePush(getSeg(table.kindOf(tempId)), table.indexOf(tempId));
                compileExpression();
                checkSymbol(']');
                vmwriter.writeArithmetic(VMWriter.Operation.ADD);
                vmwriter.writePop(VMWriter.Segment.POINTER, 1);
                vmwriter.writePush(VMWriter.Segment.THAT, 0);
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                    (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
                tokenizer.decrementPointer();
                tokenizer.decrementPointer();
                compileSubroutineCall();
            } else {
                tokenizer.decrementPointer();
                vmwriter.writePush(getSeg(table.kindOf(tempId)), table.indexOf(tempId));
            }
        } else {
            if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONSTANT) {
                vmwriter.writePush(VMWriter.Segment.CONST, tokenizer.intVal());
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONSTANT) {
                String str = tokenizer.stringVal();
                vmwriter.writePush(VMWriter.Segment.CONST, str.length());
                vmwriter.writeCall("String.new", 1);
                for (int i = 0; i < str.length(); i++) {
                    vmwriter.writePush(VMWriter.Segment.CONST, (int) str.charAt(i));
                    vmwriter.writeCall("String.appendChar", 2);
                }
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                    tokenizer.keyWord() == JackTokenizer.Keyword.TRUE) {
                vmwriter.writePush(VMWriter.Segment.CONST, 0);
                vmwriter.writeArithmetic(VMWriter.Operation.NOT);
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                    tokenizer.keyWord() == JackTokenizer.Keyword.THIS) {
                vmwriter.writePush(VMWriter.Segment.POINTER, 0);
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                    (tokenizer.keyWord() == JackTokenizer.Keyword.FALSE ||
                            tokenizer.keyWord() == JackTokenizer.Keyword.NULL)) {
                vmwriter.writePush(VMWriter.Segment.CONST, 0);
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
                compileExpression();
                checkSymbol(')');
            } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL &&
                    (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
                char s = tokenizer.symbol();
                compileTerm();
                if (s == '-') {
                    vmwriter.writeArithmetic(VMWriter.Operation.NEG);
                } else {
                    vmwriter.writeArithmetic(VMWriter.Operation.NOT);
                }
            }
        }
    }

    /**
     * Compiles a subroutine call.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileSubroutineCall() throws IOException {
        tokenizer.advance();
        String name = tokenizer.identifier();
        int nargs = 0;
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '(') {
            vmwriter.writePush(VMWriter.Segment.POINTER, 0);
            nargs = compileExpressionList() + 1;
            checkSymbol(')');
            vmwriter.writeCall(CurrentclassName + '.' + name, nargs);
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == '.') {
            String objName = name;
            tokenizer.advance();
            name = tokenizer.identifier();
            String type = table.typeOf(objName);

            if (type.equals("")) {
                name = objName + "." + name;
            } else {
                nargs = 1;
                vmwriter.writePush(getSeg(table.kindOf(objName)), table.indexOf(objName));
                name = table.typeOf(objName) + "." + name;
            }
            checkSymbol('(');
            nargs += compileExpressionList();
            checkSymbol(')');
            vmwriter.writeCall(name, nargs);
        }
    }

    /**
     * Compiles an expression.
     *
     * @throws IOException if there is an error during tokenization or writing.
     */
    private void compileExpression() throws IOException {
        compileTerm();
        do {
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.isOperator()) {

                String opCm = "";
                switch (tokenizer.symbol()) {
                    case '+':
                        opCm = "add";
                        break;
                    case '-':
                        opCm = "sub";
                        break;
                    case '*':
                        opCm = "call Math.multiply 2";
                        break;
                    case '/':
                        opCm = "call Math.divide 2";
                        break;
                    case '<':
                        opCm = "lt";
                        break;
                    case '>':
                        opCm = "gt";
                        break;
                    case '=':
                        opCm = "eq";
                        break;
                    case '&':
                        opCm = "and";
                        break;
                    case '|':
                        opCm = "or";
                        break;
                }
                compileTerm();
                vmwriter.writeCommand(opCm, "", "");
            } else {
                tokenizer.decrementPointer();
                break;
            }
        } while (true);
    }

    /**
     * Compiles a list of expressions.
     *
     * @return The number of arguments in the list.
     * @throws IOException if there is an error during tokenization or writing.
     */
    private int compileExpressionList() throws IOException {
        int nargs = 0;
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.decrementPointer();
        } else {
            nargs = 1;
            tokenizer.decrementPointer();
            compileExpression();
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL && tokenizer.symbol() == ',') {
                    compileExpression();
                    nargs++;
                } else {
                    tokenizer.decrementPointer();
                    break;
                }
            } while (true);
        }
        return nargs;
    }
}

