import frontend.*;
import frontend.Error;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Compiler {

    public static String inputFileName = "testfile.txt";

    public static String outputFileName = "error.txt";

    public static void main(String[] args) throws Exception {
        clearOutputFile();
        errorTest();
    }

    public static void errorTest() throws Exception {
        String sourceCode = input();
        Lexer lexer = new Lexer();
        List<Token> tokenList = lexer.getTokenList(sourceCode);
        Tokens tokens = new Tokens(tokenList);
        Parser parser = new Parser(tokens);
        Node root = parser.entry();
        List<Error> errors = Visitor.getInstance().entry(root);
        Collections.sort(errors);
        for (Error error : errors) {
            output(error.line + " " + error.errorType.toString());
        }
    }

    public static void parserTest() throws Exception {
        String sourceCode = input();
        Lexer lexer = new Lexer();
        List<Token> tokenList = lexer.getTokenList(sourceCode);
        Tokens tokens = new Tokens(tokenList);
        Parser parser = new Parser(tokens);
        Node root = parser.entry();
        func1(root);
    }

    public static void lexerTest() throws Exception {
        String sourceCode = input();
        Lexer lexer = new Lexer();
        List<Token> tokenList = lexer.getTokenList(sourceCode);
        for (Token token : tokenList) {
            output(token.toString());
        }
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
            output(node.type.toString() + " " + node.name);
        } else {
            if (node.type != Node.NodeType.BlockItem && node.type != Node.NodeType.Decl && node.type != Node.NodeType.BType){
                output("<" + node.type.toString() + ">");
            }
        }
    }

    public static void clearOutputFile() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, false));
        writer.write("");
        writer.close();
    }

    public static String input() throws Exception {
        byte[] fileBytes = Files.readAllBytes(Paths.get(inputFileName));
        return new String(fileBytes);
    }

    public static void output(String content) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, true));
        writer.write(content);
        writer.newLine();
        writer.close();
    }

}
