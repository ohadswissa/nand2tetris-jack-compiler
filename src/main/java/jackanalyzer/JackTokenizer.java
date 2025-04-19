package jackanalyzer;

import java.io.*;
import java.util.*;
/**
 * The JackTokenizer class breaks a .jack file into individual tokens,
 * which are the smallest meaningful elements in the Jack language.
 *
 * Tokens include:
 * - KEYWORD: Words like "class", "method", "if", "while", etc.
 * - SYMBOL: Characters like '{', '}', '=', '+', etc.
 * - IDENTIFIER: Names of variables, classes, methods, etc.
 * - INT_CONST: Numbers like 123.
 * - STRING_CONST: Text in quotes, like "hello".
 *
 * Key Features:
 * - Removes all comments from the file.
 * - Processes the file and stores all tokens in a list.
 * - Provides methods to navigate through tokens and retrieve their type and value.
 *
 * How it works:
 * 1. Reads the .jack file during initialization.
 * 2. Splits the file into tokens and stores them in a list.
 * 3. Allows to navigate and access tokens with methods like hasMoreTokens() and advance().
 */
public class JackTokenizer {

    // Enums for TokenType and Keyword
    public enum TokenType {
        KEYWORD,
        SYMBOL,
        INT_CONSTANT,
        STRING_CONSTANT,
        IDENTIFIER,
        NONE // For ease of use later on
    }

    public enum Keyword {
        CLASS,
        METHOD,
        FUNCTION,
        CONSTRUCTOR,
        INT,
        BOOLEAN,
        CHAR,
        VOID,
        VAR,
        STATIC,
        FIELD,
        LET,
        DO,
        IF,
        ELSE,
        WHILE,
        RETURN,
        TRUE,
        FALSE,
        NULL,
        THIS
    }

    private BufferedReader reader; // Reads the input file line by line.
    private String currentToken; // Holds the current token
    private List<String> tokens; // All the extracted tokens.
    private int tokenIndex; // Current token index in the tokens list.
    private boolean insideBlockComment = false; // For removing comments method.

    // Saving the predefined Jack keywords in a HashMap for fast access.
    private static final HashMap<String, Keyword> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("class", Keyword.CLASS);
        KEYWORDS.put("method", Keyword.METHOD);
        KEYWORDS.put("function", Keyword.FUNCTION);
        KEYWORDS.put("constructor", Keyword.CONSTRUCTOR);
        KEYWORDS.put("int", Keyword.INT);
        KEYWORDS.put("boolean", Keyword.BOOLEAN);
        KEYWORDS.put("char", Keyword.CHAR);
        KEYWORDS.put("void", Keyword.VOID);
        KEYWORDS.put("var", Keyword.VAR);
        KEYWORDS.put("static", Keyword.STATIC);
        KEYWORDS.put("field", Keyword.FIELD);
        KEYWORDS.put("let", Keyword.LET);
        KEYWORDS.put("do", Keyword.DO);
        KEYWORDS.put("if", Keyword.IF);
        KEYWORDS.put("else", Keyword.ELSE);
        KEYWORDS.put("while", Keyword.WHILE);
        KEYWORDS.put("return", Keyword.RETURN);
        KEYWORDS.put("true", Keyword.TRUE);
        KEYWORDS.put("false", Keyword.FALSE);
        KEYWORDS.put("null", Keyword.NULL);
        KEYWORDS.put("this", Keyword.THIS);
    }

    // Saving the predefined Jack symbols.
    private static final Set<Character> SYMBOLS = Set.of(
            '{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~'
    );

    /**
     * Constructor for initializing the tokenizer and extracting tokens from the input file.
     *
     * @param inputFile as the Jack file needed to be tokenized.
     * @throws IOException if the file cannot be read.
     */
    public JackTokenizer(File inputFile) throws IOException {
        reader = new BufferedReader(new FileReader(inputFile));
        tokens = new ArrayList<>(); // Initialization for the tokens list.
        tokenIndex = 0; // Initialization for the token index, corresponding to the beginning of the token list.
        tokenizeFile(); // Tokenizes the file.
        if (!tokens.isEmpty()) {
            currentToken = tokens.get(0); // The first token set to be the current token if it exists.
        }
    }

    /**
     * Checks if there are more tokens inside the given input.
     * @return true if there are more tokens, else false.
     */
    public boolean hasMoreTokens() {
        return tokenIndex < tokens.size();
    }

    /**
     * Getting the next token and set it to be the current token only if hasMoreToken() == true.
     */
    public void advance() {
        if (hasMoreTokens()) {
            currentToken = tokens.get(tokenIndex++);
        }
    }

    /**
     * @return the type of the current token as a constant.
     */
    public TokenType tokenType() {
        if (KEYWORDS.containsKey(currentToken)) {
            return TokenType.KEYWORD;
        } else if (SYMBOLS.contains(currentToken.charAt(0))) {
            return TokenType.SYMBOL;
        } else if (currentToken.matches("\\d+")) { // Matches digits (0-9).
            return TokenType.INT_CONSTANT;
        } else if (currentToken.startsWith("\"") && currentToken.endsWith("\"")) { // String constant.
            return TokenType.STRING_CONSTANT;
        } else {
            return TokenType.IDENTIFIER;
        }
    }

    /**
     * @return the keyword which is the current token. This method should be called only if tokenType is KEYWORD.
     */
    public Keyword keyWord() {
        if (tokenType() == TokenType.KEYWORD) {
            return KEYWORDS.get(currentToken);
        } else {
            throw new IllegalStateException("This token is NOT a keyword!");
        }
    }

    /**
     * @return the character which is the current token. Should be called only if tokenType is symbol.
     */
    public char symbol() {
        if (tokenType() == TokenType.SYMBOL) {
            return this.currentToken.charAt(0);
        } else {
            throw new IllegalStateException("This token is NOT a symbol!");
        }
    }

    /**
     * @return the string which is the current token. Should be called only if tokenType is identifier.
     */
    public String identifier() {
        if (tokenType() == TokenType.IDENTIFIER) {
            return currentToken;
        } else {
            throw new IllegalStateException("This token is not an identifier!");
        }
    }

    /**
     * @return the int value of the current token. Should be called only if tokenType() is INT_CONST.
     */
    public int intVal() {
        if (tokenType() == TokenType.INT_CONSTANT) {
            return Integer.parseInt(currentToken);
        } else {
            throw new IllegalStateException("This token is NOT an int value");
        }
    }

    /**
     * @return the string value of the current token. Should be called only if tokenType() is STRING_CONST.
     */
    public String stringVal() {
        if (tokenType() == TokenType.STRING_CONSTANT) {
            return currentToken.substring(1, currentToken.length() - 1); // Remove quotes
        } else {
            throw new IllegalStateException("This token is NOT a string value");
        }
    }

    /**
     * Reads the input file line by line and takes care of comments. After removing them, tokenize it.
     *
     * @throws IOException if an error arises while reading the file.
     */
    private void tokenizeFile() throws IOException {
        String line = reader.readLine();
        while (line != null) {
            line = removeComments(line).trim();
            if (!line.isEmpty()) {
                tokens.addAll(tokenizeLine(line));
            }
            line = reader.readLine();
        }
        reader.close();
    }

    /**
     * Helper function to remove comments from the lines.
     */
    private String removeComments(String line) {
        if (insideBlockComment) {
            if (line.contains("*/")) {
                line = line.substring(line.indexOf("*/") + 2);
                insideBlockComment = false;
            } else {
                return "";
            }
        }

        if (line.contains("//")) {
            line = line.substring(0, line.indexOf("//"));
        }

        if (line.contains("/*")) {
            if (line.contains("*/")) {
                line = line.substring(0, line.indexOf("/*")) + line.substring(line.indexOf("*/") + 2);
            } else {
                line = line.substring(0, line.indexOf("/*"));
                insideBlockComment = true;
            }
        }

        return line.trim();
    }

    /**
     * Tokenizes a single line of Jack code into a list of tokens.
     */
    private List<String> tokenizeLine(String line) {
        List<String> tokensLine = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isWhitespace(c)) {
                if (token.length() > 0) {
                    tokensLine.add(token.toString());
                    token.setLength(0);
                }
            } else if (SYMBOLS.contains(c)) {
                if (token.length() > 0) {
                    tokensLine.add(token.toString());
                    token.setLength(0);
                }
                tokensLine.add(String.valueOf(c));
            } else if (c == '"') {
                int endIdx = line.indexOf('"', i + 1);
                if (endIdx != -1) {
                    tokensLine.add(line.substring(i, endIdx + 1));
                    i = endIdx;
                }
            } else {
                token.append(c);
            }
        }
        if (token.length() > 0) {
            tokensLine.add(token.toString());
        }
        return tokensLine;
    }

    public String getCurrentToken() {
        return currentToken;
    }
    /**
     * Checks if the current token is an operator.
     * @return true if the current token is a valid operator, false otherwise.
     */
    boolean isOperator() {
        return "+-*/&|<>=".contains(currentToken);
    }

    /**
     * For decrementing purposes through the compilation process.
     */
    public void decrementPointer() {
        if (tokenIndex > 0) {
            tokenIndex--;
            currentToken = tokens.get(tokenIndex);
        }
    }
}
