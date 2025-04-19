package jackanalyzer;
import java.io.File;

public class TokenizerTest {
    public static void main(String[] args) {
        try {
            File inputFile = new File("Square/Main.jack"); // Path to your test file
            JackTokenizer tokenizer = new JackTokenizer(inputFile);

            System.out.println("=== Tokens and Their Types ===");
            while (tokenizer.hasMoreTokens()) {
                System.out.println("Token: " + tokenizer.getCurrentToken() + ", Type: " + tokenizer.tokenType());
                tokenizer.advance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
