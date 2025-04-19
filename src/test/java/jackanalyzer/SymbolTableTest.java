package jackanalyzer;

public class SymbolTableTest {
    public static void main(String[] args) {
        SymbolTable table = new SymbolTable();

        // Define class-level variables
        table.define("x", "int", SymbolTable.Symbol.Kind.STATIC);
        table.define("y", "boolean", SymbolTable.Symbol.Kind.FIELD);

        // Define subroutine-level variables
        table.define("arg1", "String", SymbolTable.Symbol.Kind.ARG);
        table.define("var1", "int", SymbolTable.Symbol.Kind.VAR);

        // Test class-level variables
        System.out.println("Kind of 'x': " + table.kindOf("x")); // STATIC
        System.out.println("Type of 'x': " + table.typeOf("x")); // int
        System.out.println("Index of 'x': " + table.indexOf("x")); // 0

        System.out.println("Kind of 'y': " + table.kindOf("y")); // FIELD
        System.out.println("Type of 'y': " + table.typeOf("y")); // boolean
        System.out.println("Index of 'y': " + table.indexOf("y")); // 0

        // Test subroutine-level variables
        System.out.println("Kind of 'arg1': " + table.kindOf("arg1")); // ARG
        System.out.println("Type of 'arg1': " + table.typeOf("arg1")); // String
        System.out.println("Index of 'arg1': " + table.indexOf("arg1")); // 0

        System.out.println("Kind of 'var1': " + table.kindOf("var1")); // VAR
        System.out.println("Type of 'var1': " + table.typeOf("var1")); // int
        System.out.println("Index of 'var1': " + table.indexOf("var1")); // 0

        // Test variable not found
        System.out.println("Kind of 'z': " + table.kindOf("z")); // NONE
        System.out.println("Type of 'z': " + table.typeOf("z")); // NONE
        System.out.println("Index of 'z': " + table.indexOf("z")); // -1

        // Reset subroutine scope and test
        table.startSubroutine();
        System.out.println("Kind of 'arg1' after reset: " + table.kindOf("arg1")); // NONE
    }
}
