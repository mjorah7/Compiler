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
        if (tokens.getCurToken().getTokenType() == tokenType) {
            Node leafNode = new Node(tokens.getCurToken().getTokenContent(), null, Node.NodeType.valueOf(tokenType.toString()));
            tokens.getNextToken();
            return leafNode;
        } else {
            // TODO error
        }
        return null;
    }

    public Node parseCompUnit () {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
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
        return new Node("CompUnit", nodes, Node.NodeType.CompUnit);
    }

    public Node parseDecl () {
        // Decl → ConstDecl | VarDecl
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.CONSTTK) {
            nodes.add(this.parseConstDecl());
        } else {
            nodes.add(this.parseVarDecl());
        }
        return new Node("Decl", nodes, Node.NodeType.Decl);
    }

    public Node parseConstDecl () {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.CONSTTK));
        nodes.add(this.parseBType());
        nodes.add(this.parseConstDef());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
            nodes.add(addLeaf(Token.TokenType.COMMA));
            nodes.add(this.parseConstDef());
        }
        nodes.add(addLeaf(Token.TokenType.SEMICN));
        return new Node("ConstDecl", nodes, Node.NodeType.ConstDecl);
    }

    public Node parseBType () {
        // BType → 'int'
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.INTTK));
        return new Node("BType", nodes, Node.NodeType.BType);
    }

    public Node parseConstDef () {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.IDENFR));
        while (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACK) {
            nodes.add(addLeaf(Token.TokenType.LBRACK));
            nodes.add(this.parseConstExp());
            nodes.add(addLeaf(Token.TokenType.RBRACK));
        }
        nodes.add(addLeaf(Token.TokenType.ASSIGN));
        nodes.add(this.parseConstInitVal());
        return new Node("ConstDef", nodes, Node.NodeType.ConstDef);
    }

    public Node parseConstInitVal () {
        // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
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
        return new Node("ConstInitVal", nodes, Node.NodeType.ConstInitVal);
    }

    public Node parseVarDecl () {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseBType());
        nodes.add(this.parseVarDef());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
            nodes.add(addLeaf(Token.TokenType.COMMA));
            nodes.add(this.parseVarDef());
        }
        nodes.add(addLeaf(Token.TokenType.SEMICN));
        return new Node("VarDecl", nodes, Node.NodeType.VarDecl);
    }

    public Node parseVarDef () {
        // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
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
        return new Node("VarDef", nodes, Node.NodeType.VarDef);
    }

    public Node parseInitVal () {
        // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
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
        return new Node("InitVal", nodes, Node.NodeType.InitVal);
    }

    public Node parseFuncDef () {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseFuncType());
        nodes.add(addLeaf(Token.TokenType.IDENFR));
        nodes.add(addLeaf(Token.TokenType.LPARENT));
        if (tokens.getCurToken().getTokenType() != Token.TokenType.RPARENT) {
            nodes.add(this.parseFuncFParams());
        }
        nodes.add(addLeaf(Token.TokenType.RPARENT));
        nodes.add(this.parseBlock());
        return new Node("FuncDef", nodes, Node.NodeType.FuncDef);
    }

    public Node parseMainFuncDef () {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.INTTK));
        nodes.add(addLeaf(Token.TokenType.MAINTK));
        nodes.add(addLeaf(Token.TokenType.LPARENT));
        nodes.add(addLeaf(Token.TokenType.RPARENT));
        nodes.add(this.parseBlock());
        return new Node("MainFuncDef", nodes, Node.NodeType.MainFuncDef);
    }

    public Node parseFuncType () {
        // FuncType → 'void' | 'int'
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.VOIDTK) {
            nodes.add(addLeaf(Token.TokenType.VOIDTK));
        } else {
            nodes.add(addLeaf(Token.TokenType.INTTK));
        }
        return new Node("FuncType", nodes, Node.NodeType.FuncType);
    }

    public Node parseFuncFParams () {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseFuncFParam());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
            nodes.add(addLeaf(Token.TokenType.COMMA));
            nodes.add(this.parseFuncFParam());
        }
        return new Node("FuncFParams", nodes, Node.NodeType.FuncFParams);
    }

    public Node parseFuncFParam () {
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
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
        return new Node("FuncFParam", nodes, Node.NodeType.FuncFParam);
    }

    public Node parseBlock () {
        // Block → '{' { BlockItem } '}'
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.LBRACE));
        while (tokens.getCurToken().getTokenType() != Token.TokenType.RBRACE) {
            nodes.add(this.parseBlockItem());
        }
        nodes.add(addLeaf(Token.TokenType.RBRACE));
        return new Node("Block", nodes, Node.NodeType.Block);
    }

    public Node parseBlockItem () {
        // BlockItem → Decl | Stmt
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.CONSTTK || tokens.getCurToken().getTokenType() == Token.TokenType.INTTK) {
            nodes.add(this.parseDecl());
        } else {
            nodes.add(this.parseStmt());
        }
        return new Node("BlockItem", nodes, Node.NodeType.BlockItem);
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
        return new Node("Stmt", nodes, Node.NodeType.Stmt);
    }

    public Node parseForStmt () {
        // ForStmt → LVal '=' Exp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseLVal());
        nodes.add(addLeaf(Token.TokenType.ASSIGN));
        nodes.add(this.parseExp());
        return new Node("ForStmt", nodes, Node.NodeType.ForStmt);
    }

    public Node parseExp () {
        // Exp → AddExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseAddExp());
        return new Node("Exp", nodes, Node.NodeType.Exp);
    }

    public Node parseCond () {
        // Cond → LOrExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseLOrExp());
        return new Node("Cond", nodes, Node.NodeType.Cond);
    }

    public Node parseLVal () {
        // LVal → Ident {'[' Exp ']'}
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.IDENFR));
        while (tokens.getCurToken().getTokenType() == Token.TokenType.LBRACK) {
            nodes.add(addLeaf(Token.TokenType.LBRACK));
            nodes.add(this.parseExp());
            nodes.add(addLeaf(Token.TokenType.RBRACK));
        }
        return new Node("LVal", nodes, Node.NodeType.LVal);
    }

    public Node parsePrimaryExp () {
        // PrimaryExp → '(' Exp ')' | LVal | Number
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
        return new Node("PrimaryExp", nodes, Node.NodeType.PrimaryExp);
    }

    public Node parseNumber () {
        // Number → IntConst
        List<Node> nodes = new ArrayList<>();
        nodes.add(addLeaf(Token.TokenType.INTCON));
        return new Node("Number", nodes, Node.NodeType.Number);
    }

    public Node parseUnaryExp () {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        List<Node> nodes = new ArrayList<>();
        if (tokens.getCurToken().getTokenType() == Token.TokenType.PLUS || tokens.getCurToken().getTokenType() == Token.TokenType.MINU || tokens.getCurToken().getTokenType() == Token.TokenType.NOT) {
            nodes.add(this.parseUnaryOp());
            nodes.add(this.parseUnaryExp());
        } else if (tokens.remainSize() >= 3 && tokens.getCurToken().getTokenType() == Token.TokenType.IDENFR && tokens.forward(1).getTokenType() == Token.TokenType.LPARENT) {
            nodes.add(addLeaf(Token.TokenType.IDENFR));
            nodes.add(addLeaf(Token.TokenType.LPARENT));
            if (tokens.getCurToken().getTokenType() != Token.TokenType.RPARENT) {
                nodes.add(this.parseFuncRParams());
            }
            nodes.add(addLeaf(Token.TokenType.RPARENT));
        } else {
            nodes.add(this.parsePrimaryExp());
        }
        return new Node("UnaryExp", nodes, Node.NodeType.UnaryExp);
    }

    public Node parseUnaryOp () {
        // UnaryOp → '+' | '−' | '!'
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
        return new Node("UnaryOp", nodes, Node.NodeType.UnaryOp);
    }

    public Node parseFuncRParams () {
        // FuncRParams → Exp { ',' Exp }
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.COMMA) {
            nodes.add(addLeaf(Token.TokenType.COMMA));
            nodes.add(this.parseExp());
        }
        return new Node("FuncRParams", nodes, Node.NodeType.FuncRParams);
    }

    public Node parseMulExp () {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseUnaryExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.MULT || tokens.getCurToken().getTokenType() == Token.TokenType.DIV || tokens.getCurToken().getTokenType() == Token.TokenType.MOD) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node("MulExp", _nodes, Node.NodeType.MulExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseUnaryExp());
        }
        return new Node("MulExp", nodes, Node.NodeType.MulExp);
    }

    public Node parseAddExp () {
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseMulExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.PLUS || tokens.getCurToken().getTokenType() == Token.TokenType.MINU) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node("AddExp", _nodes, Node.NodeType.AddExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseMulExp());
        }
        return new Node("AddExp", nodes, Node.NodeType.AddExp);
    }

    public Node parseRelExp () {
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseAddExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.GRE || tokens.getCurToken().getTokenType() == Token.TokenType.LSS || tokens.getCurToken().getTokenType() == Token.TokenType.GEQ || tokens.getCurToken().getTokenType() == Token.TokenType.LEQ) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node("RelExp", _nodes, Node.NodeType.RelExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseAddExp());
        }
        return new Node("RelExp", nodes, Node.NodeType.RelExp);
    }

    public Node parseEqExp () {
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseRelExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.EQL || tokens.getCurToken().getTokenType() == Token.TokenType.NEQ) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node("EqExp", _nodes, Node.NodeType.EqExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseRelExp());
        }
        return new Node("EqExp", nodes, Node.NodeType.EqExp);
    }

    public Node parseLAndExp () {
        // LAndExp → EqExp | LAndExp '&&' EqExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseEqExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.AND) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node("LAndExp", _nodes, Node.NodeType.LAndExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseEqExp());
        }
        return new Node("LAndExp", nodes, Node.NodeType.LAndExp);
    }

    public Node parseLOrExp () {
        // LOrExp → LAndExp | LOrExp '||' LAndExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseLAndExp());
        while (tokens.getCurToken().getTokenType() == Token.TokenType.OR) {
            List<Node> _nodes = new ArrayList<>(nodes);
            nodes.clear();
            Node newNode = new Node("LOrExp", _nodes, Node.NodeType.LOrExp);
            nodes.add(newNode);
            nodes.add(addLeaf(tokens.getCurToken().getTokenType()));
            nodes.add(this.parseLAndExp());
        }
        return new Node("LOrExp", nodes, Node.NodeType.LOrExp);
    }

    public Node parseConstExp () {
        // ConstExp → AddExp
        List<Node> nodes = new ArrayList<>();
        nodes.add(this.parseAddExp());
        return new Node("ConstExp", nodes, Node.NodeType.ConstExp);
    }

}
