package frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node {

    public String name;
    public NodeType type;
    public List<Node> nodeList;
    public int nodeLine;
    private SymbolTable symbolTable;

    private String value;

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
        this.symbolTable = null;
    }

    public Node (int line, String name, List<Node> nodeList, NodeType type, SymbolTable symbolTable) {
        this.nodeLine  = line;
        this.name = name;
        this.nodeList = nodeList;
        this.type = type;
        this.symbolTable = symbolTable;
    }

    public SymbolTable getSymbolTable () {
        return this.symbolTable;
    }

    public void setSymbolTable (SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public List<Node> getSubNodesByType (Node.NodeType nodeType) {
        return this.nodeList.stream().filter(x -> (x != null && x.type == nodeType)).collect(Collectors.toCollection(ArrayList::new));
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean subNodesContain(Node.NodeType nodeType) {
        List<Node> subNodes = this.getSubNodesByType(nodeType);
        return subNodes.size() != 0;
    }

    public Node copy() {
        List<Node> subList = new ArrayList<>();
        if (this.nodeList != null) {
            for (Node n : this.nodeList) {
                subList.add(n.copy());
            }
        }
        Node newNode = new Node(this.nodeLine, this.name, subList, this.type, this.symbolTable);
        newNode.value = this.value;
        return newNode;
    }

}
