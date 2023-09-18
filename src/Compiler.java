import frontend.Lexer;
import frontend.Token;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Compiler {

    public static void main(String[] args) throws Exception {
        lexerText();
    }

    public static String input(String filename) throws Exception {
        byte[] fileBytes = Files.readAllBytes(Paths.get(filename));
        return new String(fileBytes);
    }

    public static void output(String filename, String content) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
        writer.write(content);
        writer.newLine();
        writer.close();
    }

    public static void lexerText() throws Exception {
        String sourceCode = input("testfile.txt");
        Lexer lexer = new Lexer();
        List<Token> tokenList = lexer.parseSourceCode(sourceCode);
        for (Token token : tokenList) {
            output("output.txt", token.toString());
        }
    }

}
