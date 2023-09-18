package frontend;

import java.util.HashMap;
import java.util.Map;

public class Token {

    public enum TokenType {
        IDENFR, INTCON, STRCON, MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK,
        NOT, AND, OR, FORTK, GETINTTK, PRINTFTK, RETURNTK, PLUS, MINU, VOIDTK,
        MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ,
        ASSIGN, SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE
    }

    public static Map<String, TokenType> KeyWord = new HashMap<>() {
        {
            put("main", TokenType.MAINTK);
            put("const", TokenType.CONSTTK);
            put("int", TokenType.INTTK);
            put("break", TokenType.BREAKTK);
            put("continue", TokenType.CONTINUETK);
            put("if", TokenType.IFTK);
            put("else", TokenType.ELSETK);
            put("void", TokenType.VOIDTK);
            put("getint", TokenType.GETINTTK);
            put("printf", TokenType.PRINTFTK);
            put("return", TokenType.RETURNTK);
            put("for", TokenType.FORTK);
        }
    };

    private final int tokenLine;
    private final TokenType tokenType;
    private final String tokenContent;

    public Token(int tokenLine, TokenType tokenType, String tokenContent) {
        this.tokenLine = tokenLine;
        this.tokenType = tokenType;
        this.tokenContent = tokenContent;
    }

    public String toString() {
        return this.tokenType.toString() + " " + this.tokenContent;
    }

    public int getTokenLine() {
        return tokenLine;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getTokenContent() {
        return tokenContent;
    }

}
