package frontend;

import java.util.ArrayList;
import java.util.List;

public class Node {

    public String name;
    public NodeType type;
    public List<Node> nodeList;

    public int nodeLine;

    public enum NodeType {
        CompUnit, Decl, ConstDecl, BType, ConstDef, ConstInitVal, VarDecl, VarDef, InitVal, FuncDef,
        MainFuncDef, FuncType, FuncFParams, FuncFParam, Block, BlockItem, Stmt, ForStmt, Exp, Cond, LVal, PrimaryExp, Number, UnaryExp, UnaryOp, FuncRParams, MulExp,
        AddExp, RelExp, EqExp, LAndExp, LOrExp, ConstExp,
        IDENFR, INTCON, STRCON, MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK,
        NOT, AND, OR, FORTK, GETINTTK, PRINTFTK, RETURNTK, PLUS, MINU, VOIDTK,
        MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ,
        ASSIGN, SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE,
        REPEATTK, UNTILTK, HEXCON,
    }

    public Node (int line, String name, List<Node> nodeList, NodeType type) {
        this.nodeLine  = line;
        this.name = name;
        this.nodeList = nodeList;
        this.type = type;
    }

}
