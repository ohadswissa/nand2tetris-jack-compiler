package jackanalyzer;
import java.util.*;

/**
 * SymbolTable manages variables and their attributes within a program.
 * It keeps track of the variables defined at both the class and method levels.
 * Provides methods to define variables, look up their attributes, and manage scopes (class vs method).
 */
public class SymbolTable {

    private HashMap<String, Symbol> classSymbols; // Stores class-level variables (STATIC, FIELD)
    private HashMap<String, Symbol> methodSymbols; // Stores method-level variables (ARG, VAR)
    private HashMap<Symbol.Kind, Integer> kindIndices; // Maps variable kinds to their indices

    /**
     * Constructor initializes the class symbols, method symbols, and kind indices.
     */
    public SymbolTable() {
        classSymbols = new HashMap<>();
        methodSymbols = new HashMap<>();
        kindIndices = new HashMap<>();

        // Initialize the kind indices for different variable types
        kindIndices.put(Symbol.Kind.ARG, 0);
        kindIndices.put(Symbol.Kind.FIELD, 0);
        kindIndices.put(Symbol.Kind.STATIC, 0);
        kindIndices.put(Symbol.Kind.VAR, 0);
    }

    /**
     * Resets the method-level symbols and indices when starting a new subroutine.
     * Clears out method symbols and resets indices for argument and local variables.
     */
    public void startSubroutine() {
        methodSymbols.clear(); // Reset method-level symbols
        kindIndices.put(Symbol.Kind.VAR, 0); // Reset the index for local variables
        kindIndices.put(Symbol.Kind.ARG, 0); // Reset the index for arguments
    }

    /**
     * Defines a new variable, storing its name, type, kind, and index.
     * The variable is stored in either the class-level or method-level symbol table depending on its kind.
     *
     * @param name The variable's name.
     * @param type The variable's type (e.g., int, boolean).
     * @param kind The kind of variable (STATIC, FIELD, ARG, VAR).
     */
    public void define(String name, String type, Symbol.Kind kind) {
        int index = kindIndices.get(kind); // Get the current index for the kind
        Symbol symbol = new Symbol(type, kind, index); // Create the symbol with the given info

        // Depending on the kind, either store the symbol in the class or method table
        if (kind == Symbol.Kind.ARG || kind == Symbol.Kind.VAR) {
            methodSymbols.put(name, symbol); // Method-level variables
        } else {
            classSymbols.put(name, symbol); // Class-level variables (STATIC, FIELD)
        }

        // Update the index for the kind (increment it for the next variable)
        kindIndices.put(kind, index + 1);
    }

    /**
     * Returns the number of variables of a specific kind.
     *
     * @param kind The kind of variable (e.g., ARG, VAR).
     * @return The number of variables of the given kind.
     */
    public int varCount(Symbol.Kind kind) {
        return kindIndices.get(kind); // Return the count of variables of the given kind
    }

    /**
     * Looks up the kind of a variable by its name.
     *
     * @param name The name of the variable.
     * @return The kind of the variable (STATIC, FIELD, ARG, VAR), or NONE if not found.
     */
    public Symbol.Kind kindOf(String name) {
        Symbol symbol = findSymbol(name); // Search for the symbol by name
        return symbol != null ? symbol.getKind() : Symbol.Kind.NONE; // Return the kind or NONE if not found
    }

    /**
     * Looks up the type of a variable by its name.
     *
     * @param name The name of the variable.
     * @return The type of the variable (e.g., int, boolean), or an empty string if not found.
     */
    public String typeOf(String name) {
        Symbol symbol = findSymbol(name); // Search for the symbol by name
        return symbol != null ? symbol.getType() : ""; // Return the type or empty string if not found
    }

    /**
     * Looks up the index of a variable by its name.
     *
     * @param name The name of the variable.
     * @return The index of the variable, or -1 if not found.
     */
    public int indexOf(String name) {
        Symbol symbol = findSymbol(name); // Search for the symbol by name
        return symbol != null ? symbol.getIndex() : -1; // Return the index or -1 if not found
    }

    /**
     * Searches for a symbol in both the class and method symbol tables.
     *
     * @param name The name of the symbol.
     * @return The Symbol object if found, or null if not found.
     */
    private Symbol findSymbol(String name) {
        Symbol symbol = classSymbols.get(name); // First check the class symbols
        if (symbol != null) {
            return symbol; // Return the symbol if found in class symbols
        }
        return methodSymbols.get(name); // Otherwise, check the method symbols
    }

    /**
     * Symbol represents a single variable in the symbol table.
     * It stores the variable's type, kind (STATIC, FIELD, ARG, VAR), and index.
     */
    public static class Symbol {
        public enum Kind {
            STATIC, FIELD, ARG, VAR, NONE // Different kinds of variables
        }

        private Kind kind; // The kind of the variable (STATIC, FIELD, ARG, VAR)
        private String type; // The type of the variable (e.g., int, boolean)
        private int index; // The index of the variable in its respective kind

        // Constructor for creating a symbol
        public Symbol(String type, Kind kind, int index) {
            this.kind = kind;
            this.type = type;
            this.index = index;
        }

        // Getter methods for accessing symbol properties
        public Kind getKind() {
            return this.kind;
        }

        public String getType() {
            return this.type;
        }

        public int getIndex() {
            return this.index;
        }
    }
}
