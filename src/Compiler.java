import frontend.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Compiler {

    public static void main(String[] args) throws Exception {
        parserTest();
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

    public static void lexerTest() throws Exception {
        String sourceCode = input("testfile.txt");
        Lexer lexer = new Lexer();
        List<Token> tokenList = lexer.getTokenList(sourceCode);
        for (Token token : tokenList) {
            output("output.txt", token.toString());
        }
    }

    public static void parserTest() throws Exception {
        String sourceCode = input("testfile.txt");
        Lexer lexer = new Lexer();
        List<Token> tokenList = lexer.getTokenList(sourceCode);
        Tokens tokens = new Tokens(tokenList);
        Parser parser = new Parser(tokens);
        Node root = parser.entry();
        func1(root);
    }

    public static void func1(Node root) throws Exception {
        if (root == null) {
            return ;
        }
        if (root.nodeList != null && !root.nodeList.isEmpty()) {
            for (Node node : root.nodeList) {
                func1(node);
            }
        }
        func2(root);
    }

    public static void func2(Node node) throws Exception {
        if (node.nodeList == null || node.nodeList.isEmpty()) {
            output("output.txt", node.type.toString() + " " + node.name);
        } else {
            if (node.type != Node.NodeType.BlockItem && node.type != Node.NodeType.Decl && node.type != Node.NodeType.BType){
                output("output.txt", "<" + node.type.toString() + ">");
            }
        }
    }

}
