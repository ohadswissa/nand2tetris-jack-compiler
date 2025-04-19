# Jack Compiler - Nand2Tetris Project 11

This project is the final stage of the [Nand2Tetris](https://www.nand2tetris.org/) course.  
It implements a complete **Jack-to-VM compiler** in Java, translating high-level Jack programs into low-level virtual machine code.

---

## 🛠 Features

- **Tokenizer** – Breaks `.jack` source into tokens (keywords, symbols, identifiers, etc.)
- **Parser / CompilationEngine** – Recursively compiles complete syntax tree
- **Symbol Table** – Tracks class-level and subroutine-level identifiers
- **VMWriter** – Generates stack-based VM commands (push, pop, call, label, etc.)
- Handles:
  - Class and subroutine declarations
  - Control flow (`if`, `while`, `return`)
  - Expressions and operator precedence
  - Let/var/argument/field/this usage
  - Object-oriented constructs

---

## 📂 Key Files & Structure

- `JackTokenizer.java` – Token stream processor  
- `CompilationEngine.java` – Recursive descent parser  
- `SymbolTable.java` – Manages variable scope and memory segments  
- `VMWriter.java` – Writes VM commands from syntax tree  
- `JackAnalyzer.java` – Entry point: handles I/O and file traversal  
- `src/test/java/` – Includes unit tests and test `.jack` files  
- `pom.xml` – Maven project setup  
- `.gitignore` – Clean project structure

---

## ▶️ How to Run

```bash
mvn compile
mvn exec:java -Dexec.mainClass="jackanalyzer.JackAnalyzer" -Dexec.args="test.vm"
```
To test your output, you can use a **diff checker** (like [https://www.diffchecker.com](https://www.diffchecker.com))  
by comparing your generated `.vm` file with the provided reference `.vm` file.

## 🧪 Example Input (Jack)
```
class Main {
  function void main() {
    do Output.printString("Hello Jack");
    return;
  }
}
```

## ➡️ Output (VM)
```
push constant 11
call String.new 1
push constant 72
call String.appendChar 2
...
call Output.printString 1
pop temp 0
push constant 0
return
```
## 👨‍💻 Author

Ohad Swissa
Honors Student – Computer Science & Entrepreneurship
Ex-IDF Special Forces Major | Problem Solver
[LinkedIn](https://www.linkedin.com/in/ohad-swissa-54728a2a6)
