package frontend;

import mid.IrGenerator;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

public class Visitor {

    private static Visitor instance = new Visitor();

    public static Visitor getInstance () {
        return instance;
    }

    private List<Error> errors;

    private Node rootNode;

    private SymbolTable rootSymbolTable;

    private SymbolTable curSymbolTable;

    private boolean isInFunc = false;

    private boolean isIntFunc = false;

    private int loopCnt = 0;

    private int level = 0;

    private Visitor () {
        this.rootSymbolTable = new SymbolTable();
        this.curSymbolTable = this.rootSymbolTable;
        this.errors = new ArrayList<>();
    }

    public List<Error> entry (Node rootNode) {
        this.rootNode = rootNode;

        IrGenerator.refactorLoop(rootNode);
        while (!IrGenerator.branchReducedOr) {
            IrGenerator.branchReducedOr = true;
            IrGenerator.refactorBranchOr(rootNode);
        }
        while (!IrGenerator.branchReducedAnd) {
            IrGenerator.branchReducedAnd = true;
            IrGenerator.refactorBranchAnd(rootNode);
        }

        checkCompUnitError(this.rootNode);
        removeDuplicate();
        rootNode.setSymbolTable(rootSymbolTable);

        Collections.sort(errors);
        return errors;
    }

    private void removeDuplicate() {
        Set<Error> setErrors = new HashSet<>(errors);
        errors = new ArrayList<>(setErrors);
    }

    public void addError (Error error) {
        this.errors.add(error);
    }

    private List<Node> getSubNodesByType (Node node, Node.NodeType nodeType) {
        return node.nodeList.stream().filter(x -> (x != null && x.type == nodeType)).collect(Collectors.toCollection(ArrayList::new));
    }

    private void createSubSymbolTable () {
        SymbolTable newSymbolTable = new SymbolTable();
        newSymbolTable.father = curSymbolTable;
        curSymbolTable = newSymbolTable;
    }

    private void returnToLastSymbolTable() {
        curSymbolTable = curSymbolTable.father;
    }

    private void setNodeSymbolTable (Node node) {
        node.setSymbolTable(this.curSymbolTable);
    }

    private void checkCompUnitError (Node node) {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        List<Node> DeclNodes = getSubNodesByType(node, Node.NodeType.Decl);
        List<Node> FuncDefNodes = getSubNodesByType(node, Node.NodeType.FuncDef);
        List<Node> MainFuncDefNodes = getSubNodesByType(node, Node.NodeType.MainFuncDef);
        for (Node Decl : DeclNodes) {
            checkDeclError(Decl);
        }
        for (Node FuncDef : FuncDefNodes) {
            checkFuncDefError(FuncDef);
        }
        for (Node MainFuncDef : MainFuncDefNodes) {
            checkMainFuncDefError(MainFuncDef);
        }

        setNodeSymbolTable(node);
    }

    private void checkDeclError (Node node) {
        // Decl → ConstDecl | VarDecl
        List<Node> ConstDeclNodes = getSubNodesByType(node, Node.NodeType.ConstDecl);
        List<Node> VarDeclNodes = getSubNodesByType(node, Node.NodeType.VarDecl);
        for (Node ConstDecl : ConstDeclNodes) {
            checkConstDeclError(ConstDecl);
        }
        for (Node VarDecl : VarDeclNodes) {
            checkVarDeclError(VarDecl);
        }

        setNodeSymbolTable(node);
    }

    private void checkConstDeclError (Node node) {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
        List<Node> BTypeNodes = getSubNodesByType(node, Node.NodeType.BType);
        List<Node> ConstDefNodes = getSubNodesByType(node, Node.NodeType.ConstDef);
        for (Node BType : BTypeNodes) {
            checkBTypeError(BType);
        }
        for (Node ConstDef : ConstDefNodes) {
            checkConstDefError(ConstDef);
        }

        setNodeSymbolTable(node);
    }

    private void checkBTypeError (Node node) {
        // BType → 'int'

        setNodeSymbolTable(node);
    }

    private void checkConstDefError (Node node) {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal  // b k
        List<Node> IdentNodes = getSubNodesByType(node, Node.NodeType.IDENFR);
        List<Node> ConstExpNodes = getSubNodesByType(node, Node.NodeType.ConstExp);
        List<Node> ConstInitValNodes = getSubNodesByType(node, Node.NodeType.ConstInitVal);
        if (curSymbolTable.table.containsKey(IdentNodes.get(0).name)) {
            addError(new Error(IdentNodes.get(0).nodeLine, Error.ErrorType.b));
        }
        for (Node ConstExp : ConstExpNodes) {
            checkConstExpError(ConstExp);
        }

        // add to symbol table
        Symbol.VarType varType = Symbol.VarType.VAR;
        if (ConstExpNodes.size() == 1) {
            varType = Symbol.VarType.ARRAY1;
        }
        if (ConstExpNodes.size() == 2) {
            varType = Symbol.VarType.ARRAY2;
        }
        curSymbolTable.addSymbol(IdentNodes.get(0).name, new Symbol(
                IdentNodes.get(0).name,
                varType,
                Symbol.ValueType.INT,
                true,
                null,
                level
        ));

        for (Node ConstInitVal : ConstInitValNodes) {
            checkConstInitValError(ConstInitVal);
        }

        setNodeSymbolTable(node);
    }

    private void checkConstInitValError (Node node) {
        // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        List<Node> ConstExpNodes = getSubNodesByType(node, Node.NodeType.ConstExp);
        List<Node> ConstInitValNodes = getSubNodesByType(node, Node.NodeType.ConstInitVal);
        if (ConstExpNodes.size() == 0) {
            for (Node ConstInitVal : ConstInitValNodes) {
                checkConstInitValError(ConstInitVal);
            }
        } else {
            for (Node ConstExp : ConstExpNodes) {
                checkConstExpError(ConstExp);
            }
        }

        setNodeSymbolTable(node);
    }

    private void checkVarDeclError (Node node) {
        // VarDecl → BType VarDef { ',' VarDef } ';' // i
        List<Node> BTypeNodes = getSubNodesByType(node, Node.NodeType.BType);
        List<Node> VarDefNodes = getSubNodesByType(node, Node.NodeType.VarDef);
        for (Node BType : BTypeNodes) {
            checkBTypeError(BType);
        }
        for (Node VarDef : VarDefNodes) {
            checkVarDefError(VarDef);
        }

        setNodeSymbolTable(node);
    }

    private void checkVarDefError (Node node) {
        // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal // b k
        List<Node> IdentNodes = getSubNodesByType(node, Node.NodeType.IDENFR);
        List<Node> ConstExpNodes = getSubNodesByType(node, Node.NodeType.ConstExp);
        List<Node> InitValNodes = getSubNodesByType(node, Node.NodeType.InitVal);
        if (curSymbolTable.table.containsKey(IdentNodes.get(0).name)) {
            addError(new Error(IdentNodes.get(0).nodeLine, Error.ErrorType.b));
        }
        for (Node ConstExp : ConstExpNodes) {
            checkConstExpError(ConstExp);
        }

        // add to symbol table
        Symbol.VarType varType = Symbol.VarType.VAR;
        if (ConstExpNodes.size() == 1) {
            varType = Symbol.VarType.ARRAY1;
        }
        if (ConstExpNodes.size() == 2) {
            varType = Symbol.VarType.ARRAY2;
        }
        curSymbolTable.addSymbol(IdentNodes.get(0).name, new Symbol(
                IdentNodes.get(0).name,
                varType,
                Symbol.ValueType.INT,
                false,
                null,
                level
        ));

        for (Node InitVal : InitValNodes) {
            checkInitValError(InitVal);
        }

        setNodeSymbolTable(IdentNodes.get(0));

        setNodeSymbolTable(node);
    }

    private void checkInitValError (Node node) {
        // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        List<Node> ExpNodes = getSubNodesByType(node, Node.NodeType.Exp);
        List<Node> InitValNodes = getSubNodesByType(node, Node.NodeType.InitVal);
        for (Node Exp : ExpNodes) {
            checkExpError(Exp);
        }
        for (Node InitVal : InitValNodes) {
            checkInitValError(InitVal);
        }

        setNodeSymbolTable(node);
    }

    private boolean justAfterFuncDeclare = false;

    private void checkFuncDefError (Node node) {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g j
        List<Node> FuncTypeNodes = getSubNodesByType(node, Node.NodeType.FuncType);
        List<Node> IdentNodes = getSubNodesByType(node, Node.NodeType.IDENFR);
        List<Node> FuncFParamsNodes = getSubNodesByType(node, Node.NodeType.FuncFParams);
        List<Node> BlockNodes = getSubNodesByType(node, Node.NodeType.Block);
        for (Node FuncType : FuncTypeNodes) {
            checkFuncTypeError(FuncType);
        }
        if (curSymbolTable.table.containsKey(IdentNodes.get(0).name)) {
            addError(new Error(IdentNodes.get(0).nodeLine, Error.ErrorType.b));
        }

        // add func name to symbol table
        curSymbolTable.addSymbol(IdentNodes.get(0).name, new Symbol(
                IdentNodes.get(0).name,
                Symbol.VarType.FUNC,
                Objects.equals(FuncTypeNodes.get(0).nodeList.get(0).name, "int") ? Symbol.ValueType.INT : Symbol.ValueType.VOID,
                false,
                FuncFParamsNodes.size() > 0 ? FuncFParamsToList(FuncFParamsNodes.get(0)) : null,
                level
        ));

        setNodeSymbolTable(node);

        // create a new symbol table for func
        createSubSymbolTable();
        this.isInFunc = true;
        this.isIntFunc = Objects.equals(FuncTypeNodes.get(0).nodeList.get(0).name, "int");

        for (Node FuncFParams : FuncFParamsNodes) {
            checkFuncFParamsError(FuncFParams);
        }
        justAfterFuncDeclare = true;
        for (Node Block : BlockNodes) {
            checkBlockError(Block);
        }

//        returnToLastSymbolTable();

        this.isInFunc = false;
        this.isIntFunc = false;
    }

    private void checkMainFuncDefError (Node node) {
        // MainFuncDef → 'int' 'main' '(' ')' Block // g j
        List<Node> BlockNodes = getSubNodesByType(node, Node.NodeType.Block);
        this.isInFunc = true;
        this.isIntFunc = true;

        curSymbolTable.addSymbol("main", new Symbol(
                "main",
                Symbol.VarType.FUNC,
                Symbol.ValueType.VOID,
                false,
                null,
                level
        ));
        setNodeSymbolTable(node);
        createSubSymbolTable();

        for (Node Block : BlockNodes) {
            checkBlockError(Block);
        }
        this.isInFunc = false;
        this.isIntFunc = false;
    }

    private void checkFuncTypeError (Node node) {
        // FuncType → 'void' | 'int'

        setNodeSymbolTable(node);
    }

    private void checkFuncFParamsError (Node node) {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        List<Node> FuncFParamNodes = getSubNodesByType(node, Node.NodeType.FuncFParam);
        for (Node FuncFParam : FuncFParamNodes) {
            checkFuncFParamError(FuncFParam);
        }

        setNodeSymbolTable(node);
    }

    private void checkFuncFParamError (Node node) {
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]  //   b k
        List<Node> BTypeNodes = getSubNodesByType(node, Node.NodeType.BType);
        List<Node> IdentNodes = getSubNodesByType(node, Node.NodeType.IDENFR);
        List<Node> ConstExpNodes = getSubNodesByType(node, Node.NodeType.ConstExp);
        for (Node BType : BTypeNodes) {
            checkBTypeError(BType);
        }
        if (curSymbolTable.table.containsKey(IdentNodes.get(0).name)) {
            addError(new Error(IdentNodes.get(0).nodeLine, Error.ErrorType.b));
        }
        for (Node ConstExp : ConstExpNodes) {
            checkConstExpError(ConstExp);
        }

        Symbol.VarType varType = Symbol.VarType.VAR;
        if (getSubNodesByType(node, Node.NodeType.LBRACK).size() == 1) {
            varType = Symbol.VarType.ARRAY1;
        }
        if (getSubNodesByType(node, Node.NodeType.LBRACK).size() == 2) {
            varType = Symbol.VarType.ARRAY2;
        }
        curSymbolTable.addSymbol(IdentNodes.get(0).name, new Symbol(
                IdentNodes.get(0).name,
                varType,
                Symbol.ValueType.INT,
                false,
                null,
                level
        ));

        setNodeSymbolTable(node);
    }

    private void checkBlockError (Node node) {
        // Block → '{' { BlockItem } '}'
        List<Node> BlockItemNodes = getSubNodesByType(node, Node.NodeType.BlockItem);

        setNodeSymbolTable(node);

        // create a new symbol table for block, mind the exception for func
        level ++;
        if (!justAfterFuncDeclare) {
            createSubSymbolTable();
        }

        justAfterFuncDeclare = false;

        for (Node BlockItem : BlockItemNodes) {
            checkBlockItemError(BlockItem);
        }

        // error g
        if (level == 1 && this.isInFunc && this.isIntFunc) {
            if (BlockItemNodes.size() == 0) {
                addError(new Error(node.nodeList.get(node.nodeList.size()-1).nodeLine, Error.ErrorType.g));
            } else {
                Node lastBlockItemNode = BlockItemNodes.get(BlockItemNodes.size() - 1);
                if (lastBlockItemNode.nodeList.get(0).type != Node.NodeType.Stmt ||
                        lastBlockItemNode.nodeList.get(0).type == Node.NodeType.Stmt && lastBlockItemNode.nodeList.get(0).nodeList.get(0).type != Node.NodeType.RETURNTK) {
                    addError(new Error(node.nodeList.get(node.nodeList.size() - 1).nodeLine, Error.ErrorType.g));
                }
            }
        }

        returnToLastSymbolTable();
        level --;
    }

    private void checkBlockItemError (Node node) {
        // BlockItem → Decl | Stmt
        List<Node> DeclNodes = getSubNodesByType(node, Node.NodeType.Decl);
        List<Node> StmtNodes = getSubNodesByType(node, Node.NodeType.Stmt);
        for (Node Decl : DeclNodes) {
            checkDeclError(Decl);
        }
        for (Node Stmt : StmtNodes) {
            checkStmtError(Stmt);
        }

        setNodeSymbolTable(node);
    }

    private void checkStmtError (Node node) {
        /*
        Stmt → LVal '=' Exp ';' // h
        | [Exp] ';'
        | Block
        | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        | 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        | 'break' ';' | 'continue' ';' // m
        | 'return' [Exp] ';' // f
        | LVal '=' 'getint''('')'';' // h
        | 'printf''('FormatString{,Exp}')'';' // l
        */
        if (node.nodeList.get(0).type == Node.NodeType.IFTK) {
            List<Node> CondNodes = getSubNodesByType(node, Node.NodeType.Cond);
            List<Node> StmtNodes = getSubNodesByType(node, Node.NodeType.Stmt);
            for (Node Cond : CondNodes) {
                checkCondError(Cond);
            }
            for (Node Stmt : StmtNodes) {
                checkStmtError(Stmt);
            }
        } else if (node.nodeList.get(0).type == Node.NodeType.FORTK) {
            List<Node> ForStmtNodes = getSubNodesByType(node, Node.NodeType.ForStmt);
            List<Node> CondNodes = getSubNodesByType(node, Node.NodeType.Cond);
            List<Node> StmtNodes = getSubNodesByType(node, Node.NodeType.Stmt);
            for (Node ForStmt : ForStmtNodes) {
                checkForStmtError(ForStmt);
            }
            for (Node Cond : CondNodes) {
                checkCondError(Cond);
            }
            loopCnt ++;
            for (Node Stmt : StmtNodes) {
                checkStmtError(Stmt);
            }
            loopCnt --;
        } else if (node.nodeList.get(0).type == Node.NodeType.BREAKTK) {
            if (loopCnt <= 0) {
                addError(new Error(node.nodeList.get(0).nodeLine, Error.ErrorType.m));
            }
        } else if (node.nodeList.get(0).type == Node.NodeType.CONTINUETK) {
            if (loopCnt <= 0) {
                addError(new Error(node.nodeList.get(0).nodeLine, Error.ErrorType.m));
            }
        } else if (node.nodeList.get(0).type == Node.NodeType.RETURNTK) {
            List<Node> ExpNodes = getSubNodesByType(node, Node.NodeType.Exp);
            if (level == 1 && isInFunc && !isIntFunc) {
                if (node.nodeList.size() >= 3) {
                    addError(new Error(node.nodeList.get(0).nodeLine, Error.ErrorType.f));
                }
            }
            for (Node Exp : ExpNodes) {
                checkExpError(Exp);
            }
        } else if (node.nodeList.get(0).type == Node.NodeType.PRINTFTK) {
            List<Node> ExpNodes = getSubNodesByType(node, Node.NodeType.Exp);
            int cnt = 0;
            for (int i = 0 ; i < node.nodeList.get(2).name.length() ; i ++) {
                if (node.nodeList.get(2).name.charAt(i) == '%' && node.nodeList.get(2).name.charAt(i+1) == 'd') {
                    cnt ++;
                }
            }
            if (cnt != ExpNodes.size()) {
                addError(new Error(node.nodeList.get(0).nodeLine, Error.ErrorType.l));
            }
            for (Node Exp : ExpNodes) {
                checkExpError(Exp);
            }
        } else {
            // LVal '=' Exp ';' // h
            // [Exp] ';'
            // Block
            // LVal '=' 'getint''('')'';' // h
            List<Node> LValNodes = getSubNodesByType(node, Node.NodeType.LVal);
            List<Node> ExpNodes = getSubNodesByType(node, Node.NodeType.Exp);
            List<Node> BlockNodes = getSubNodesByType(node, Node.NodeType.Block);
            if (LValNodes.size() > 0 && curSymbolTable.containsSymbol(LValNodes.get(0).nodeList.get(0).name) && curSymbolTable.getVisableSymbol(LValNodes.get(0).nodeList.get(0).name).isConst) {
                addError(new Error(LValNodes.get(0).nodeLine, Error.ErrorType.h));
            }
            for (Node LVal : LValNodes) {
                checkLValError(LVal);
            }
            for (Node Exp : ExpNodes) {
                checkExpError(Exp);
            }
            for (Node Block : BlockNodes) {
                checkBlockError(Block);
            }
        }

        setNodeSymbolTable(node);
    }

    private void checkForStmtError (Node node) {
        // ForStmt → LVal '=' Exp   //h
        List<Node> LValNodes = getSubNodesByType(node, Node.NodeType.LVal);
        List<Node> ExpNodes = getSubNodesByType(node, Node.NodeType.Exp);

        // error h
        if (curSymbolTable.containsSymbol(LValNodes.get(0).nodeList.get(0).name) && curSymbolTable.getVisableSymbol(LValNodes.get(0).nodeList.get(0).name).isConst) {
            addError(new Error(LValNodes.get(0).nodeLine, Error.ErrorType.h));
        }

        for (Node LVal : LValNodes) {
            checkLValError(LVal);
        }
        for (Node Exp : ExpNodes) {
            checkExpError(Exp);
        }

        setNodeSymbolTable(node);
    }

    private void checkExpError (Node node) {
        // Exp → AddExp
        List<Node> AddExpNodes = getSubNodesByType(node, Node.NodeType.AddExp);
        for (Node AddExp : AddExpNodes) {
            checkAddExpError(AddExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkCondError (Node node) {
        // Cond → LOrExp
        List<Node> LOrExpNodes = getSubNodesByType(node, Node.NodeType.LOrExp);
        for (Node LOrExp : LOrExpNodes) {
            checkLOrExpError(LOrExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkLValError (Node node) {
        // LVal → Ident {'[' Exp ']'} // c k
        List<Node> IdentNodes = getSubNodesByType(node, Node.NodeType.IDENFR);
        List<Node> ExpNodes = getSubNodesByType(node, Node.NodeType.Exp);
        if (curSymbolTable.getVisableSymbol(IdentNodes.get(0).name) == null) {
            addError(new Error(IdentNodes.get(0).nodeLine, Error.ErrorType.c));
        }
        for (Node Exp : ExpNodes) {
            checkExpError(Exp);
        }

        setNodeSymbolTable(node);
    }

    private void checkPrimaryExpError (Node node) {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        List<Node> ExpNodes = getSubNodesByType(node, Node.NodeType.Exp);
        List<Node> LValNodes = getSubNodesByType(node, Node.NodeType.LVal);
        List<Node> NumberNodes = getSubNodesByType(node, Node.NodeType.Number);
        for (Node Exp : ExpNodes) {
            checkExpError(Exp);
        }
        for (Node LVal : LValNodes) {
            checkLValError(LVal);
        }
        for (Node Number : NumberNodes) {
            checkNumberError(Number);
        }

        setNodeSymbolTable(node);
    }

    private void checkNumberError (Node node) {
        // Number → IntConst

        setNodeSymbolTable(node);
    }

    private void checkUnaryExpError (Node node) {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // c d e j
        List<Node> PrimaryExpNodes = getSubNodesByType(node, Node.NodeType.PrimaryExp);
        List<Node> IdentNodes = getSubNodesByType(node, Node.NodeType.IDENFR);
        List<Node> FuncRParamsNodes = getSubNodesByType(node, Node.NodeType.FuncRParams);
        List<Node> UnaryOpNodes = getSubNodesByType(node, Node.NodeType.UnaryOp);
        List<Node> UnaryExpNodes = getSubNodesByType(node, Node.NodeType.UnaryExp);
        for (Node PrimaryExp : PrimaryExpNodes) {
            checkPrimaryExpError(PrimaryExp);
        }
        if (IdentNodes.size() > 0 && curSymbolTable.getVisableSymbol(IdentNodes.get(0).name) == null) {
            addError(new Error(IdentNodes.get(0).nodeLine, Error.ErrorType.c));
        }
        if (IdentNodes.size() > 0 &&
                (getParamsNum(IdentNodes.get(0).name) != 0 && FuncRParamsNodes.size() == 0 ||
                        FuncRParamsNodes.size() > 0 && (FuncRParamsNodes.get(0).nodeList.size() + 1) / 2 != getParamsNum(IdentNodes.get(0).name))) {
            addError(new Error(IdentNodes.get(0).nodeLine, Error.ErrorType.d));
        } else if (IdentNodes.size() > 0 && FuncRParamsNodes.size() > 0) {
            List<Symbol.VarType> fParamsVarType = curSymbolTable.getVisableSymbol(IdentNodes.get(0).name).paramsType;
            List<Symbol.VarType> rParamsVarType = FuncRParamsToList(FuncRParamsNodes.get(0));
            boolean flag = false;
            for (int i = 0; i < fParamsVarType.size(); i++) {
                if (fParamsVarType.get(i) != rParamsVarType.get(i)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                addError(new Error(IdentNodes.get(0).nodeLine, Error.ErrorType.e));
            }
        }

        for (Node UnaryOp : UnaryOpNodes) {
            checkUnaryOpError(UnaryOp);
        }
        for (Node UnaryExp : UnaryExpNodes) {
            checkUnaryExpError(UnaryExp);
        }
        for (Node FuncRParams : FuncRParamsNodes) {
            checkFuncRParamsError(FuncRParams);
        }

        setNodeSymbolTable(node);
    }

    private void checkUnaryOpError (Node node) {
        //  UnaryOp → '+' | '−' | '!'

        setNodeSymbolTable(node);
    }

    private void checkFuncRParamsError (Node node) {
        // FuncRParams → Exp { ',' Exp }
        List<Node> ExpNodes = getSubNodesByType(node, Node.NodeType.Exp);
        for (Node Exp : ExpNodes) {
            checkExpError(Exp);
        }

        setNodeSymbolTable(node);
    }

    private void checkMulExpError (Node node) {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        List<Node> UnaryExpNodes = getSubNodesByType(node, Node.NodeType.UnaryExp);
        List<Node> MulExpNodes = getSubNodesByType(node, Node.NodeType.MulExp);
        for (Node UnaryExp : UnaryExpNodes) {
            checkUnaryExpError(UnaryExp);
        }
        for (Node MulExp : MulExpNodes) {
            checkMulExpError(MulExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkAddExpError (Node node) {
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        List<Node> MulExpNodes = getSubNodesByType(node, Node.NodeType.MulExp);
        List<Node> AddExpNodes = getSubNodesByType(node, Node.NodeType.AddExp);
        for (Node MulExp : MulExpNodes) {
            checkMulExpError(MulExp);
        }
        for (Node AddExp : AddExpNodes) {
            checkAddExpError(AddExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkRelExpError (Node node) {
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        List<Node> AddExpNodes = getSubNodesByType(node, Node.NodeType.AddExp);
        List<Node> RelExpNodes = getSubNodesByType(node, Node.NodeType.RelExp);
        for (Node AddExp : AddExpNodes) {
            checkAddExpError(AddExp);
        }
        for (Node RelExp : RelExpNodes) {
            checkRelExpError(RelExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkEqExpError (Node node) {
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        List<Node> RelExpNodes = getSubNodesByType(node, Node.NodeType.RelExp);
        List<Node> EqExpNodes = getSubNodesByType(node, Node.NodeType.EqExp);
        for (Node RelExp : RelExpNodes) {
            checkRelExpError(RelExp);
        }
        for (Node EqExp : EqExpNodes) {
            checkEqExpError(EqExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkLAndExpError (Node node) {
        // LAndExp → EqExp | LAndExp '&&' EqExp
        List<Node> EqExpNodes = getSubNodesByType(node, Node.NodeType.EqExp);
        List<Node> LAndExpNodes = getSubNodesByType(node, Node.NodeType.LAndExp);
        for (Node EqExp : EqExpNodes) {
            checkEqExpError(EqExp);
        }
        for (Node LAndExp : LAndExpNodes) {
            checkLAndExpError(LAndExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkLOrExpError (Node node) {
        // LOrExp → LAndExp | LOrExp '||' LAndExp
        List<Node> LAndExpNodes = getSubNodesByType(node, Node.NodeType.LAndExp);
        List<Node> LOrExpNodes = getSubNodesByType(node, Node.NodeType.LOrExp);
        for (Node LAndExp : LAndExpNodes) {
            checkLAndExpError(LAndExp);
        }
        for (Node LOrExp : LOrExpNodes) {
            checkLOrExpError(LOrExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkConstExpError (Node node) {
        // ConstExp → AddExp
        List<Node> AddExpNodes = getSubNodesByType(node, Node.NodeType.AddExp);
        for (Node AddExp : AddExpNodes) {
            checkAddExpError(AddExp);
        }

        setNodeSymbolTable(node);
    }

    private void checkFormatStringError (Node node) {
        // <FormatString> → '"'{<Char>}'"' // a

        setNodeSymbolTable(node);
    }

    // below are helper func
    private int getParamsNum (String funcName) {
        Symbol symbol = rootSymbolTable.table.get(funcName);
        if (symbol == null) {
            return 0;
        }
        return symbol.paramsType == null ? 0 : symbol.paramsType.size();
    }

    private List<Symbol.VarType> getParamsTypes (String funcName) {
        Symbol symbol = rootSymbolTable.table.get(funcName);
        return symbol.paramsType;
    }

    private List<Symbol.VarType> FuncFParamsToList (Node FuncFParamsNode) {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        List<Symbol.VarType> ret = new ArrayList<>();
        List<Node> FuncFParamNodes = getSubNodesByType(FuncFParamsNode, Node.NodeType.FuncFParam);
        for (Node FuncFParam : FuncFParamNodes) {
            if (FuncFParam.nodeList.stream().filter(x -> (x.type == Node.NodeType.LBRACK)).count() == 2) {
                ret.add(Symbol.VarType.ARRAY2);
            } else if (FuncFParam.nodeList.stream().filter(x -> (x.type == Node.NodeType.LBRACK)).count() == 1) {
                ret.add(Symbol.VarType.ARRAY1);
            } else {
                ret.add(Symbol.VarType.VAR);
            }
        }
        return ret;
    }

    Symbol.VarType varType = null;

    private List<Symbol.VarType> FuncRParamsToList (Node FuncRParamsNode) {
        // FuncRParams → Exp { ',' Exp }
        // Exp → AddExp
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        // PrimaryExp → '(' Exp ')' | LVal | Number
        // LVal → Ident {'[' Exp ']'}
        List<Symbol.VarType> ret = new ArrayList<>();
        for (Node Exp : FuncRParamsNode.nodeList) {
            if (Exp.type == Node.NodeType.COMMA) {
                continue;
            }
            try {
                ret.add(getExpVarType(Exp));
            } catch (Exception e) {
                ret.add(Symbol.VarType.ERROR);
            }
        }

        setNodeSymbolTable(FuncRParamsNode);

        return ret;
    }

    private Symbol.VarType getExpVarType (Node Exp) throws Exception {
        // Exp → AddExp
        setNodeSymbolTable(Exp);

        return getAddExpVarType(Exp.nodeList.get(0));
    }

    private Symbol.VarType getAddExpVarType (Node AddExp) throws Exception {
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        List<Symbol.VarType> types = new ArrayList<>();
        for (Node node : AddExp.nodeList) {
            if (node.type == Node.NodeType.AddExp) {
                types.add(getAddExpVarType(node));
            }
            if (node.type == Node.NodeType.MulExp) {
                types.add(getMulExpVarType(node));
            }
        }
        if (new HashSet<>(types).size() > 1) {
            throw new Exception();
        }

        setNodeSymbolTable(AddExp);

        return types.get(0);
    }

    private Symbol.VarType getMulExpVarType (Node MulExp) throws Exception {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        List<Symbol.VarType> types = new ArrayList<>();
        for (Node node : MulExp.nodeList) {
            if (node.type == Node.NodeType.UnaryExp) {
                types.add(getUnaryExpVarType(node));
            }
            if (node.type == Node.NodeType.MulExp) {
                types.add(getMulExpVarType(node));
            }
        }
        if (new HashSet<>(types).size() > 1) {
            throw new Exception();
        }

        setNodeSymbolTable(MulExp);

        return types.get(0);
    }

    private Symbol.VarType getUnaryExpVarType (Node UnaryExp) throws Exception {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (UnaryExp.nodeList.get(0).type == Node.NodeType.PrimaryExp) {
            return getPrimaryExpVarType(UnaryExp.nodeList.get(0));
        }
        if (UnaryExp.nodeList.get(0).type == Node.NodeType.IDENFR) {
            Symbol symbol = curSymbolTable.getVisableSymbol(UnaryExp.nodeList.get(0).name);
            return symbol.symbolValueType == Symbol.ValueType.INT ? Symbol.VarType.VAR : Symbol.VarType.ERROR;
        }
        if (UnaryExp.nodeList.get(0).type == Node.NodeType.UnaryOp) {
            return getUnaryExpVarType(UnaryExp.nodeList.get(1));
        }

        setNodeSymbolTable(UnaryExp);

        return Symbol.VarType.DEBUG;
    }

    private Symbol.VarType getPrimaryExpVarType (Node PrimaryExp) throws Exception {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        if (PrimaryExp.nodeList.get(0).type == Node.NodeType.LPARENT) {
            return getExpVarType(PrimaryExp.nodeList.get(1));
        }
        if (PrimaryExp.nodeList.get(0).type == Node.NodeType.LVal) {
            return getLValVarType(PrimaryExp.nodeList.get(0));
        }
        if (PrimaryExp.nodeList.get(0).type == Node.NodeType.Number) {
            return Symbol.VarType.VAR;
        }

        setNodeSymbolTable(PrimaryExp);

        return Symbol.VarType.DEBUG;
    }

    private Symbol.VarType getLValVarType (Node LVal) throws Exception {
        // LVal → Ident {'[' Exp ']'}  // array[1]  // array[1][1]
        Symbol symbol = curSymbolTable.getVisableSymbol(LVal.nodeList.get(0).name);
        Symbol.VarType type = symbol.symbolVarType;
        if (type == Symbol.VarType.ARRAY2) {
            if (LVal.nodeList.size() == 4) {
                type = Symbol.VarType.ARRAY1;
            }
            if (LVal.nodeList.size() == 7) {
                type = Symbol.VarType.VAR;
            }
        } else if (type == Symbol.VarType.ARRAY1) {
            if (LVal.nodeList.size() == 4) {
                type = Symbol.VarType.VAR;
            }
        }

        setNodeSymbolTable(LVal);

        return type;
    }

}
