package frontend;

import java.util.ArrayList;
import java.util.List;

import static frontend.Token.TokenType;

public class Lexer {

    private int position;
    private int line;

    public Lexer() {
        this.position = 0;
        this.line = 1;
    }

    public List<Token> getTokenList(String sourceCode) {
        String source = removeComment(sourceCode);
        List<Token> tokens = new ArrayList<>();
        Token token = nextToken(source);
        while (token != null) {
            tokens.add(token);
            token = nextToken(source);
        }
        return tokens;
    }

    public String removeComment(String sourceCode) {
        StringBuilder source = new StringBuilder(sourceCode);

        for (int pos = 0 ; pos < source.length() ; pos ++) {
            // overlook format string
            if (source.charAt(pos) == '"'){
                do{
                    pos ++;
                } while (source.charAt(pos) != '"');
                pos ++;
            }

            // replace one line comment with space
            else if (source.charAt(pos) == '/' && pos + 1 < source.length() && source.charAt(pos+1) == '/') {
                source.replace(pos, pos + 2, "  ");
                pos += 2;
                while (pos < source.length() && source.charAt(pos) != '\n') {
                    source.replace(pos, pos + 1, " ");
                    pos ++;
                }
            }

            // replace multiple line comment with space and '\n'
            else if (source.charAt(pos) == '/' && pos + 1 < source.length() && source.charAt(pos+1) == '*') {
                source.replace(pos, pos + 2, "  ");
                pos += 2;
                while (pos + 1 < source.length() && (source.charAt(pos) != '*' || source.charAt(pos+1) != '/')) {
                    if (source.charAt(pos) != '\n')
                        source.replace(pos, pos + 1, " ");
                    pos ++;
                }
                source.replace(pos, pos + 2, "  ");
                pos += 1;
            }
        }

        return source.toString();
    }

    public Token nextToken(String source) {
        StringBuilder curString = new StringBuilder();
        Token.TokenType type = null;

        while (this.position < source.length() && isBlankChar(source.charAt(this.position))) {
            if (source.charAt(this.position) == '\n' || source.charAt(this.position) == '\r') {
                this.line ++;
            }
            this.position ++;
        }

        if (this.position >= source.length()) {
            return null;
        }

        if (Character.isLetter(source.charAt(this.position)) || source.charAt(this.position) == '_') {
            do {
                curString.append(source.charAt(this.position));
                this.position ++;
            } while (Character.isLetter(source.charAt(this.position)) ||
                    Character.isDigit(source.charAt(this.position)) ||
                    source.charAt(this.position) == '_');
            type = Token.KeyWord.getOrDefault(curString.toString(), TokenType.IDENFR);
        } else if (Character.isDigit(source.charAt(this.position))) {
            do {
                curString.append(source.charAt(this.position));
                this.position ++;
            } while (Character.isDigit(source.charAt(this.position)));
            type = TokenType.INTCON;
        } else if (source.charAt(this.position) == '"') {
            do {
                curString.append(source.charAt(this.position));
                this.position ++;
            } while (source.charAt(this.position) != '"');
            curString.append(source.charAt(this.position));
            this.position ++;
            type = TokenType.STRCON;
        } else {
            switch (source.charAt(this.position)) {
                case '+' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.PLUS;
                }
                case '-' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.MINU;
                }
                case '*' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.MULT;
                }
                case '/' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.DIV;
                }
                case '%' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.MOD;
                }
                case ';' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.SEMICN;
                }
                case ',' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.COMMA;
                }
                case '(' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.LPARENT;
                }
                case ')' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.RPARENT;
                }
                case '[' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.LBRACK;
                }
                case ']' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.RBRACK;
                }
                case '{' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.LBRACE;
                }
                case '}' -> {
                    curString.append(source.charAt(this.position));
                    type = TokenType.RBRACE;
                }
                case '&' -> {
                    if (source.charAt(this.position + 1) != '&') {
                        // TODO error
                    } else {
                        curString.append(source.charAt(this.position));
                        this.position++;
                        curString.append(source.charAt(this.position));
                        type = TokenType.AND;
                    }
                }
                case '|' -> {
                    if (source.charAt(this.position + 1) != '|') {
                        // TODO error
                    } else {
                        curString.append(source.charAt(this.position));
                        this.position++;
                        curString.append(source.charAt(this.position));
                        type = TokenType.OR;
                    }
                }
                case '<' -> {
                    if (source.charAt(this.position + 1) == '=') {
                        curString.append(source.charAt(this.position));
                        this.position++;
                        curString.append(source.charAt(this.position));
                        type = TokenType.LEQ;
                    } else {
                        curString.append(source.charAt(this.position));
                        type = TokenType.LSS;
                    }
                }
                case '>' -> {
                    if (source.charAt(this.position + 1) == '=') {
                        curString.append(source.charAt(this.position));
                        this.position++;
                        curString.append(source.charAt(this.position));
                        type = TokenType.GEQ;
                    } else {
                        curString.append(source.charAt(this.position));
                        type = TokenType.GRE;
                    }
                }
                case '=' -> {
                    if (source.charAt(this.position + 1) == '=') {
                        curString.append(source.charAt(this.position));
                        this.position++;
                        curString.append(source.charAt(this.position));
                        type = TokenType.EQL;
                    } else {
                        curString.append(source.charAt(this.position));
                        type = TokenType.ASSIGN;
                    }
                }
                case '!' -> {
                    if (source.charAt(this.position + 1) == '=') {
                        curString.append(source.charAt(this.position));
                        this.position++;
                        curString.append(source.charAt(this.position));
                        type = TokenType.NEQ;
                    } else {
                        curString.append(source.charAt(this.position));
                        type = TokenType.NOT;
                    }
                }
            }
            this.position ++;
        }

        return new Token(this.line, type, curString.toString());
    }

    private boolean isBlankChar(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c== '\r';
    }

}
