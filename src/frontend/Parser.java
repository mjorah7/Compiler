package frontend;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private Tokens tokens;

    public Parser (Tokens tokens) {
        this.tokens = tokens;
    }

    public Node entry () {
        tokens.getNextToken();
        return this.parseCompUnit();
    }

    public Node addLeaf (Token.TokenType tokenType) {
        if (tokenType != Token.TokenType.STRCON && tokens.getCurToken().getTokenType() == tokenType) {
            Node leafNode = new Node(tokens.getCurToken().getTokenLine(), tokens.getCurToken().getTokenContent(), null, Node.NodeType.valueOf(tokenType.toString()));
            tokens.getNextToken();
            return leafNode;
        } else if (tokenType == Token.TokenType.STRCON && tokens.getCurToken().getTokenType() == tokenType) {
            if (checkStrCon(tokens.getCurToken().getTokenContent())) {
                Node leafNode = new Node(tokens.getCurToken().getTokenLine(), tokens.getCurToken().getTokenContent(), null, Node.NodeType.valueOf(tokenType.toString()));
                tokens.getNextToken();
                return leafNode;
            } else {
                Visitor.getInstance().addError(new Error(tokens.getCurToken().getTokenLine(), Error.ErrorType.a));
                Node leafNode = new Node(tokens.getCurToken().getTokenLine(), tokens.getCurToken().getTokenContent(), null, Node.NodeType.valueOf(tokenType.toString()));
                tokens.getNextToken();
                return leafNode;
            }
        } else {
            int line = tokens.forward(-1).getTokenLine();
            if (tokenType == Token.TokenType.SEMICN) {
                Visitor.getInstance().addError(new Error(line, Error.ErrorType.i));
                Node leafNode = new Node(line, ";", null, Node.NodeType.SEMICN);
                return leafNode;
            }
            if (tokenType == Token.TokenType.RPARENT) {
                Visitor.getInstance().addError(new Error(line, Error.ErrorType.j));
                Node leafNode = new Node(line, ")", null, Node.NodeType.RPARENT);
                return leafNode;
            }
            if (tokenType == Token.TokenType.RBRACK) {
                Visitor.getInstance().addError(new Error(line, Error.ErrorType.k));
                Node leafNode = new Node(line, "]", null, Node.NodeType.RBRACK);
                return leafNode;
            }
        }
        return null;
    }

    private boolean checkStrCon (String strCon) {
        if (strCon.charAt(0) != '\"' || strCon.charAt(strCon.length()-1) != '\"') {
            return false;
        }
        for (int pos = 1 ; pos < strCon.length()-1 ; pos ++) {
            if (pos != strCon.length()-1 && strCon.charAt(pos) == '\\' && strCon.charAt(pos+1) != 'n') {
                return false;
            }
            if (pos != strCon.length()-1 && strCon.charAt(pos) == '\\' && strCon.charAt(pos+1) == 'n') {
                continue;
            }
            if (pos != strCon.length()-1 && strCon.charAt(pos) == '%' && strCon.charAt(pos+1) != 'd') {
                return false;
            }
            if (pos != strCon.length()-1 && strCon.charAt(pos) == '%' && strCon.charAt(pos+1) == 'd') {
                continue;
            }
            if (strCon.charAt(pos) != 32 && strCon.charAt(pos) != 33 && (strCon.charAt(pos) < 40 || strCon.charAt(pos) > 126)) {
                return false;
            }
        }
        return true;
    }

    public Node parseCompUnit () {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        while (tokens.remainSize() >= 3 && (tokens.forward(0).getTokenType() == Token.TokenType.CONSTTK
                || tokens.forward(0).getTokenType() == Token.TokenType.INTTK
                && tokens.forward(1).getTokenType() == Token.TokenType.IDENFR
                && tokens.forward(2).getTokenType() != Token.TokenType.LPARENT)) {
            nodes.add(this.parseDecl());
        }
        while (tokens.remainSize() >= 3 && (tokens.forward(0).getTokenType() == Token.TokenType.INTTK
                || tokens.forward(0).getTokenType() == Token.TokenType.VOIDTK)
                && tokens.forward(1).getTokenType() != Token.TokenType.MAINTK) {
            nodes.add(this.parseFuncDef());
        }
        nodes.add(this.parseMainFuncDef());
        return new Node(line, "CompUnit", nodes, Node.NodeType.CompUnit);
    }

    public Node parseDecl () {
        // Decl → ConstDecl | VarDecl
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.CONSTTK) {
            nodes.add(this.parseConstDecl());
        } else {
            nodes.add(this.parseVarDecl());
        }
        return new Node(line, "Decl", nodes, Node.NodeType.Decl);
    }

    public Node parseConstDecl () {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.CONSTTK));
        nodes.add(this.parseBType());
        nodes.add(this.parseConstDef());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
            nodes.add(addLeaf(Token.TokenType.COMMA));
            nodes.add(this.parseConstDef());
        }
        nodes.add(addLeaf(Token.TokenType.SEMICN));
        return new Node(line, "ConstDecl", nodes, Node.NodeType.ConstDecl);
    }

    public Node parseBType () {
        // BType → 'int'
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.INTTK));
        return new Node(line, "BType", nodes, Node.NodeType.BType);
    }

    public Node parseConstDef () {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.IDENFR));
        while (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACK) {
            nodes.add(addLeaf(Token.TokenType.LBRACK));
            nodes.add(this.parseConstExp());
            nodes.add(addLeaf(Token.TokenType.RBRACK));
        }
        nodes.add(addLeaf(Token.TokenType.ASSIGN));
        nodes.add(this.parseConstInitVal());
        return new Node(line, "ConstDef", nodes, Node.NodeType.ConstDef);
    }

    public Node parseConstInitVal () {
        // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACE) {
            nodes.add(addLeaf(Token.TokenType.LBRACE));
            if (tokens.getCurToken().getTokenType() != Token.TokenType.RBRACE) {
                nodes.add(this.parseConstInitVal());
                while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
                    nodes.add(addLeaf(Token.TokenType.COMMA));
                    nodes.add(this.parseConstInitVal());
                }
            }
            nodes.add(addLeaf(Token.TokenType.RBRACE));
        } else {
            nodes.add(this.parseConstExp());
        }
        return new Node(line, "ConstInitVal", nodes, Node.NodeType.ConstInitVal);
    }

    public Node parseVarDecl () {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseBType());
        nodes.add(this.parseVarDef());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
            nodes.add(addLeaf(Token.TokenType.COMMA));
            nodes.add(this.parseVarDef());
        }
        nodes.add(addLeaf(Token.TokenType.SEMICN));
        return new Node(line, "VarDecl", nodes, Node.NodeType.VarDecl);
    }

    public Node parseVarDef () {
        // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.IDENFR));
        while (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACK) {
            nodes.add(addLeaf(Token.TokenType.LBRACK));
            nodes.add(this.parseConstExp());
            nodes.add(addLeaf(Token.TokenType.RBRACK));
        }
        if (tokens.getCurToken().getTokenType() == Token.TokenType.ASSIGN) {
            nodes.add(addLeaf(Token.TokenType.ASSIGN));
            nodes.add(this.parseInitVal());
        }
        return new Node(line, "VarDef", nodes, Node.NodeType.VarDef);
    }

    public Node parseInitVal () {
        // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACE) {
            nodes.add(addLeaf(Token.TokenType.LBRACE));
            if (tokens.getCurToken().getTokenType() != Token.TokenType.RBRACE) {
                nodes.add(this.parseInitVal());
                while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
                    nodes.add(addLeaf(Token.TokenType.COMMA));
                    nodes.add(this.parseInitVal());
                }
            }
            nodes.add(addLeaf(Token.TokenType.RBRACE));
        } else {
            nodes.add(this.parseExp());
        }
        return new Node(line, "InitVal", nodes, Node.NodeType.InitVal);
    }

    public Node parseFuncDef () {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseFuncType());
        nodes.add(addLeaf(Token.TokenType.IDENFR));
        nodes.add(addLeaf(Token.TokenType.LPARENT));
        if (tokens.getCurToken().getTokenType() != Token.TokenType.RPARENT && tokens.getCurToken().getTokenType() != Token.TokenType.LBRACE) {
            nodes.add(this.parseFuncFParams());
        }
        nodes.add(addLeaf(Token.TokenType.RPARENT));
        nodes.add(this.parseBlock());
        return new Node(line, "FuncDef", nodes, Node.NodeType.FuncDef);
    }

    public Node parseMainFuncDef () {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.INTTK));
        nodes.add(addLeaf(Token.TokenType.MAINTK));
        nodes.add(addLeaf(Token.TokenType.LPARENT));
        nodes.add(addLeaf(Token.TokenType.RPARENT));
        nodes.add(this.parseBlock());
        return new Node(line, "MainFuncDef", nodes, Node.NodeType.MainFuncDef);
    }

    public Node parseFuncType () {
        // FuncType → 'void' | 'int'
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.VOIDTK) {
            nodes.add(addLeaf(Token.TokenType.VOIDTK));
        } else {
            nodes.add(addLeaf(Token.TokenType.INTTK));
        }
        return new Node(line, "FuncType", nodes, Node.NodeType.FuncType);
    }

    public Node parseFuncFParams () {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseFuncFParam());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
            nodes.add(addLeaf(Token.TokenType.COMMA));
            nodes.add(this.parseFuncFParam());
        }
        return new Node(line, "FuncFParams", nodes, Node.NodeType.FuncFParams);
    }

    public Node parseFuncFParam () {
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseBType());
        nodes.add(addLeaf(Token.TokenType.IDENFR));
        if (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACK) {
            nodes.add(addLeaf(Token.TokenType.LBRACK));
            nodes.add(addLeaf(Token.TokenType.RBRACK));
            while (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACK) {
                nodes.add(addLeaf(Token.TokenType.LBRACK));
                nodes.add(this.parseConstExp());
                nodes.add(addLeaf(Token.TokenType.RBRACK));
            }
        }
        return new Node(line, "FuncFParam", nodes, Node.NodeType.FuncFParam);
    }

    public Node parseBlock () {
        // Block → '{' { BlockItem } '}'
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.LBRACE));
        while (tokens.getCurToken().getTokenType() != Token.TokenType.RBRACE) {
            nodes.add(this.parseBlockItem());
        }
        nodes.add(addLeaf(Token.TokenType.RBRACE));
        return new Node(line, "Block", nodes, Node.NodeType.Block);
    }

    public Node parseBlockItem () {
        // BlockItem → Decl | Stmt
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.CONSTTK || tokens.getCurToken().getTokenType() == Token.TokenType.INTTK) {
            nodes.add(this.parseDecl());
        } else {
            nodes.add(this.parseStmt());
        }
        return new Node(line, "BlockItem", nodes, Node.NodeType.BlockItem);
    }

    public Node parseStmt () {
        /*
        Stmt →  LVal '=' Exp ';'
                | [Exp] ';'
                | Block
                | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                | 'break' ';' | 'continue' ';'
                | 'return' [Exp] ';'
                | LVal '=' 'getint''('')'';'
                | 'printf''('FormatString{','Exp}')'';'
         */
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.SEMICN) {
            nodes.add(addLeaf(Token.TokenType.SEMICN));
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.IFTK) {
            nodes.add(addLeaf(Token.TokenType.IFTK));
            nodes.add(addLeaf(Token.TokenType.LPARENT));
            nodes.add(this.parseCond());
            nodes.add(addLeaf(Token.TokenType.RPARENT));
            nodes.add(this.parseStmt());
            if (tokens.getCurToken().getTokenType() == Token.TokenType.ELSETK) {
                nodes.add(addLeaf(Token.TokenType.ELSETK));
                nodes.add(this.parseStmt());
            }
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.FORTK) {
            nodes.add(addLeaf(Token.TokenType.FORTK));
            nodes.add(addLeaf(Token.TokenType.LPARENT));
            if (tokens.getCurToken().getTokenType() != Token.TokenType.SEMICN) {
                nodes.add(this.parseForStmt());
            }
            nodes.add(addLeaf(Token.TokenType.SEMICN));
            if (tokens.getCurToken().getTokenType() != Token.TokenType.SEMICN) {
                nodes.add(this.parseCond());
            }
            nodes.add(addLeaf(Token.TokenType.SEMICN));
            if (tokens.getCurToken().getTokenType() != Token.TokenType.RPARENT) {
                nodes.add(this.parseForStmt());
            }
            nodes.add(addLeaf(Token.TokenType.RPARENT));
            nodes.add(this.parseStmt());
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.BREAKTK) {
            nodes.add(addLeaf(Token.TokenType.BREAKTK));
            nodes.add(addLeaf(Token.TokenType.SEMICN));
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.CONTINUETK) {
            nodes.add(addLeaf(Token.TokenType.CONTINUETK));
            nodes.add(addLeaf(Token.TokenType.SEMICN));
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.RETURNTK) {
            nodes.add(addLeaf(Token.TokenType.RETURNTK));
            if (tokens.getCurToken().getTokenType() != Token.TokenType.SEMICN) {
                nodes.add(this.parseExp());
            }
            nodes.add(addLeaf(Token.TokenType.SEMICN));
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.PRINTFTK) {
            nodes.add(addLeaf(Token.TokenType.PRINTFTK));
            nodes.add(addLeaf(Token.TokenType.LPARENT));
            nodes.add(addLeaf(Token.TokenType.STRCON));
            while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
                nodes.add(addLeaf(Token.TokenType.COMMA));
                nodes.add(this.parseExp());
            }
            nodes.add(addLeaf(Token.TokenType.RPARENT));
            nodes.add(addLeaf(Token.TokenType.SEMICN));
        } else {
            // Stmt →  [Exp] ';' | Block | LVal '=' 'getint''('')'';' | LVal '=' Exp ';'\
            if (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACE) {
                nodes.add(this.parseBlock());
            } else {
                int position = tokens.getPosition();
                this.parseExp();
                if (tokens.getCurToken().getTokenType() == Token.TokenType.ASSIGN) {
                    tokens.setPosition(position);
                    nodes.add(this.parseLVal());
                    nodes.add(addLeaf(Token.TokenType.ASSIGN));
                    if (tokens.getCurToken().getTokenType() == Token.TokenType.GETINTTK) {
                        nodes.add(addLeaf(Token.TokenType.GETINTTK));
                        nodes.add(addLeaf(Token.TokenType.LPARENT));
                        nodes.add(addLeaf(Token.TokenType.RPARENT));
                        nodes.add(addLeaf(Token.TokenType.SEMICN));
                    } else {
                        nodes.add(this.parseExp());
                        nodes.add(addLeaf(Token.TokenType.SEMICN));
                    }
                } else {
                    tokens.setPosition(position);
                    nodes.add(this.parseExp());
                    nodes.add(addLeaf(Token.TokenType.SEMICN));
                }
            }
        }
        return new Node(line, "Stmt", nodes, Node.NodeType.Stmt);
    }

    public Node parseForStmt () {
        // ForStmt → LVal '=' Exp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseLVal());
        nodes.add(addLeaf(Token.TokenType.ASSIGN));
        nodes.add(this.parseExp());
        return new Node(line, "ForStmt", nodes, Node.NodeType.ForStmt);
    }

    public Node parseExp () {
        // Exp → AddExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseAddExp());
        return new Node(line, "Exp", nodes, Node.NodeType.Exp);
    }

    public Node parseCond () {
        // Cond → LOrExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseLOrExp());
        return new Node(line, "Cond", nodes, Node.NodeType.Cond);
    }

    public Node parseLVal () {
        // LVal → Ident {'[' Exp ']'}
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.IDENFR));
        while (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACK) {
            nodes.add(addLeaf(Token.TokenType.LBRACK));
            nodes.add(this.parseExp());
            nodes.add(addLeaf(Token.TokenType.RBRACK));
        }
        return new Node(line, "LVal", nodes, Node.NodeType.LVal);
    }

    public Node parsePrimaryExp () {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.LPARENT) {
            nodes.add(addLeaf(Token.TokenType.LPARENT));
            nodes.add(this.parseExp());
            nodes.add(addLeaf(Token.TokenType.RPARENT));
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.INTCON) {
            nodes.add(this.parseNumber());
        } else {
            nodes.add(this.parseLVal());
        }
        return new Node(line, "PrimaryExp", nodes, Node.NodeType.PrimaryExp);
    }

    public Node parseNumber () {
        // Number → IntConst
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.INTCON));
        return new Node(line, "Number", nodes, Node.NodeType.Number);
    }

    public Node parseUnaryExp () {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.PLUS || tokens.getCurToken().getTokenType() == Token.TokenType.MINU || tokens.getCurToken().getTokenType() == Token.TokenType.NOT) {
            nodes.add(this.parseUnaryOp());
            nodes.add(this.parseUnaryExp());
        } else if (tokens.remainSize() >= 3 && tokens.getCurToken().getTokenType() == Token.TokenType.IDENFR && tokens.forward(1).getTokenType() == Token.TokenType.LPARENT) {
            nodes.add(addLeaf(Token.TokenType.IDENFR));
            nodes.add(addLeaf(Token.TokenType.LPARENT));
            if (tokens.getCurToken().getTokenType() == Token.TokenType.PLUS || tokens.getCurToken().getTokenType() == Token.TokenType.MINU || tokens.getCurToken().getTokenType() == Token.TokenType.INTCON || tokens.getCurToken().getTokenType() == Token.TokenType.IDENFR) {
                nodes.add(this.parseFuncRParams());
            }
            nodes.add(addLeaf(Token.TokenType.RPARENT));
        } else {
            nodes.add(this.parsePrimaryExp());
        }
        return new Node(line, "UnaryExp", nodes, Node.NodeType.UnaryExp);
    }

    public Node parseUnaryOp () {
        // UnaryOp → '+' | '−' | '!'
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.PLUS) {
            nodes.add(addLeaf(Token.TokenType.PLUS));
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.MINU) {
            nodes.add(addLeaf(Token.TokenType.MINU));
        } else if (tokens.getCurToken().getTokenType() == Token.TokenType.NOT) {
            nodes.add(addLeaf(Token.TokenType.NOT));
        } else {
            // TODO error
        }
        return new Node(line, "UnaryOp", nodes, Node.NodeType.UnaryOp);
    }

    public Node parseFuncRParams () {
        // FuncRParams → Exp { ',' Exp }
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
            nodes.add(addLeaf(Token.TokenType.COMMA));
            nodes.add(this.parseExp());
        }
        return new Node(line, "FuncRParams", nodes, Node.NodeType.FuncRParams);
    }

    public Node parseMulExp () {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseUnaryExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.MULT || tokens.getCurToken().getTokenType() == Token.TokenType.DIV || tokens.getCurToken().getTokenType() == Token.TokenType.MOD) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node(tokens.getCurToken().getTokenLine(), "MulExp", _nodes, Node.NodeType.MulExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseUnaryExp());
        }
        return new Node(line, "MulExp", nodes, Node.NodeType.MulExp);
    }

    public Node parseAddExp () {
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseMulExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.PLUS || tokens.getCurToken().getTokenType() == Token.TokenType.MINU) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node(tokens.getCurToken().getTokenLine(), "AddExp", _nodes, Node.NodeType.AddExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseMulExp());
        }
        return new Node(line, "AddExp", nodes, Node.NodeType.AddExp);
    }

    public Node parseRelExp () {
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseAddExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.GRE || tokens.getCurToken().getTokenType() == Token.TokenType.LSS || tokens.getCurToken().getTokenType() == Token.TokenType.GEQ || tokens.getCurToken().getTokenType() == Token.TokenType.LEQ) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node(tokens.getCurToken().getTokenLine(), "RelExp", _nodes, Node.NodeType.RelExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseAddExp());
        }
        return new Node(line, "RelExp", nodes, Node.NodeType.RelExp);
    }

    public Node parseEqExp () {
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseRelExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.EQL || tokens.getCurToken().getTokenType() == Token.TokenType.NEQ) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node(tokens.getCurToken().getTokenLine(), "EqExp", _nodes, Node.NodeType.EqExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseRelExp());
        }
        return new Node(line, "EqExp", nodes, Node.NodeType.EqExp);
    }

    public Node parseLAndExp () {
        // LAndExp → EqExp | LAndExp '&&' EqExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseEqExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.AND) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node(tokens.getCurToken().getTokenLine(), "LAndExp", _nodes, Node.NodeType.LAndExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseEqExp());
        }
        return new Node(line, "LAndExp", nodes, Node.NodeType.LAndExp);
    }

    public Node parseLOrExp () {
        // LOrExp → LAndExp | LOrExp '||' LAndExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseLAndExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.OR) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node(tokens.getCurToken().getTokenLine(), "LOrExp", _nodes, Node.NodeType.LOrExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseLAndExp());
        }
        return new Node(line, "LOrExp", nodes, Node.NodeType.LOrExp);
    }

    public Node parseConstExp () {
        // ConstExp → AddExp
        int line = tokens.getCurToken().getTokenLine();
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseAddExp());
        return new Node(line, "ConstExp", nodes, Node.NodeType.ConstExp);
    }

}
