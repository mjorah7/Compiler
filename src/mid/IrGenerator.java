package mid;

import frontend.Node;
import frontend.Symbol;
import frontend.SymbolTable;
import mid.ir.*;
import mid.ir.Number;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IrGenerator {

    private final Node rootNode;
    private final SymbolTable rootSymbolTable;
    private final IrModule irModule;
    private final LabelCounter labelCounter;
    private final TempCounter tempCounter;

    private int level;
    private SymbolTable curSymbolTable;
    private FuncDef curFuncDef;
    private BasicBlock curBasicBlock;

    // for branch
    private String curBranchLabel = null;

    // for branch reduction
    public static boolean branchReducedOr = false;
    public static boolean branchReducedAnd = false;

    // for loop
    private String label1 = null;
    private String label2 = null;
    private String label3 = null;
    private String label4 = null;
    private String label5 = null;

    // for getElementPtr
    private boolean isCalling = false;

    public IrGenerator(Node rootNode, SymbolTable rootSymbolTable) {
        this.rootNode = rootNode;
        this.rootSymbolTable = rootSymbolTable;
        this.curSymbolTable = rootSymbolTable;
        this.irModule = new IrModule();
        this.labelCounter = new LabelCounter();
        this.tempCounter = new TempCounter();
        this.level = 0;
    }

    public IrModule visitCompUnit() {
        Symbol.careDefined = true;

        // CompUnit → {Decl} {FuncDef} MainFuncDef
        distributeList(this.rootNode.nodeList);
        return this.irModule;
    }

    private void distributeList(List<Node> nodes) {
        for (Node node : nodes) {
            distribute(node);
        }
    }

    private void distribute(Node node) {
        if (node.type == Node.NodeType.MainFuncDef)
            visitMainFuncDef(node);
        if (node.type == Node.NodeType.Block)
            visitBlock(node);
        if (node.type == Node.NodeType.BlockItem)
            visitBlockItem(node);
        if (node.type == Node.NodeType.Stmt)
            visitStmt(node);
        if (node.type == Node.NodeType.Exp)
            visitExp(node);
        if (node.type == Node.NodeType.Decl)
            visitDecl(node);
        if (node.type == Node.NodeType.ConstDecl)
            visitConstDecl(node);
        if (node.type == Node.NodeType.VarDecl)
            visitVarDecl(node);
        if (node.type == Node.NodeType.ConstDef)
            visitConstDef(node);
        if (node.type == Node.NodeType.VarDef)
            visitVarDef(node);
        if (node.type == Node.NodeType.FuncDef)
            visitFuncDef(node);
    }

    private void visitFuncDef(Node node) {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncDef preFuncDef = this.curFuncDef;
        node.getSymbolTable().setSymbolDefined(node.nodeList.get(1).name);
        this.curFuncDef = new FuncDef(findSymbol(node.getSubNodesByType(Node.NodeType.IDENFR).get(0).name));
        if (node.subNodesContain(Node.NodeType.FuncFParams)) {
            setFuncFParams(node.getSubNodesByType(Node.NodeType.FuncFParams).get(0));
        }

        distributeList(node.nodeList);

        // alloca VAR params
        if (curFuncDef.symbol.params != null) {
            for (Symbol symbol : curFuncDef.symbol.params) {
                if (symbol.isVar()) {
                    Symbol tmp = tempCounter.getTemp(level + 1);
                    curFuncDef.basicBlocks.get(0).addToFront(new PointerOp(PointerOp.Op.STORE, tmp, symbol));
                    curFuncDef.basicBlocks.get(0).addToFront(new VarDef(tmp));
                    symbol.paramSymbol = tmp;
                }
            }
        }

        if (findSymbol(node.getSubNodesByType(Node.NodeType.IDENFR).get(0).name).symbolValueType == Symbol.ValueType.VOID) {
            curBasicBlock.addToChain(new Return(null));
        } else {
            curBasicBlock.addToChain(new Return(new Number(0)));
        }

        this.irModule.addFuncDef(this.curFuncDef);
        this.curFuncDef = preFuncDef;
    }

    private void visitMainFuncDef(Node node) {
        // MainFuncDef → 'int' 'main' '(' ')' Block
        FuncDef preFuncDef = this.curFuncDef;
        node.getSymbolTable().setSymbolDefined("main");
        this.curFuncDef = new MainFuncDef(findSymbol("main"));

        distributeList(node.nodeList);

        this.irModule.addFuncDef(this.curFuncDef);
        this.curFuncDef = preFuncDef;
    }

    private void visitBlock(Node node) {
        // Block → '{' { BlockItem } '}'
        SymbolTable preSymbolTable = this.curSymbolTable;
        this.curSymbolTable = node.getSymbolTable();
        level ++;

        this.curBasicBlock = new BasicBlock(labelCounter.getLabel());
        this.curFuncDef.addBasicBlock(this.curBasicBlock);

        distributeList(node.nodeList);

        level --;
        this.curSymbolTable = preSymbolTable;
    }

    private void visitBlockItem(Node node) {
        // BlockItem → Decl | Stmt
        distributeList(node.nodeList);
    }

    private void visitDecl(Node node) {
        // Decl → ConstDecl | VarDecl
        distributeList(node.nodeList);
    }

    private void visitConstDecl(Node node) {
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        distributeList(node.nodeList);
    }

    private void visitVarDecl(Node node) {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        distributeList(node.nodeList);
    }

    private void visitConstDef(Node node) {
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        node.getSymbolTable().setSymbolDefined(node.nodeList.get(0).name);
        Symbol symbol = node.getSymbolTable().getVisableSymbol(node.nodeList.get(0).name);
        if (symbol.isGlobal) {
            if (symbol.symbolVarType == Symbol.VarType.VAR && node.subNodesContain(Node.NodeType.ASSIGN)) {
                symbol.globalValue = Calculator.calExp(node.getSubNodesByType(Node.NodeType.ConstInitVal).get(0).nodeList.get(0));
                symbol.initValue = new Number(symbol.globalValue);
            }
            if (symbol.symbolVarType == Symbol.VarType.ARRAY1 || symbol.symbolVarType == Symbol.VarType.ARRAY2) {
                initializeArray(symbol, node);
            }
            irModule.addVarDef(symbol);
        } else {
            VarDef varDef = new VarDef(symbol);
            curBasicBlock.addToChain(varDef);
            if (symbol.symbolVarType == Symbol.VarType.VAR && node.subNodesContain(Node.NodeType.ASSIGN)) {
                if (symbol.isConst) {
                    symbol.globalValue = Calculator.calConstExp(node.getSubNodesByType(Node.NodeType.ConstInitVal).get(0).nodeList.get(0));
                }
                Operand init = visitConstExp(node.getSubNodesByType(Node.NodeType.ConstInitVal).get(0).nodeList.get(0));
                symbol.initValue = init;
                curBasicBlock.addToChain(new PointerOp(PointerOp.Op.STORE, symbol, init));
            }
            if (symbol.symbolVarType == Symbol.VarType.ARRAY1 || symbol.symbolVarType == Symbol.VarType.ARRAY2) {
                initializeArray(symbol, node);
            }
        }
    }

    private void visitVarDef(Node node) {
        // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        node.getSymbolTable().setSymbolDefined(node.nodeList.get(0).name);
        Symbol symbol = node.getSymbolTable().getVisableSymbol(node.nodeList.get(0).name);
        if (symbol.isGlobal) {
            if (symbol.symbolVarType == Symbol.VarType.VAR && node.subNodesContain(Node.NodeType.ASSIGN)) {
                symbol.globalValue = Calculator.calExp(node.getSubNodesByType(Node.NodeType.InitVal).get(0).nodeList.get(0));
                symbol.initValue = new Number(symbol.globalValue);
            }
            if (symbol.symbolVarType == Symbol.VarType.ARRAY1 || symbol.symbolVarType == Symbol.VarType.ARRAY2) {
                initializeArray(symbol, node);
            }
            irModule.addVarDef(symbol);
        } else {
            VarDef varDef = new VarDef(symbol);
            curBasicBlock.addToChain(varDef);
            if (symbol.symbolVarType == Symbol.VarType.VAR && node.subNodesContain(Node.NodeType.ASSIGN)) {
                Operand init = visitExp(node.getSubNodesByType(Node.NodeType.InitVal).get(0).nodeList.get(0));
                symbol.initValue = init;
                curBasicBlock.addToChain(new PointerOp(PointerOp.Op.STORE, symbol, init));
            }
            if (symbol.symbolVarType == Symbol.VarType.ARRAY1 || symbol.symbolVarType == Symbol.VarType.ARRAY2) {
                initializeArray(symbol, node);
            }
        }
    }

    private void initializeArray(Symbol array, Node node) {
        if (node.type == Node.NodeType.ConstDef) {
            // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
            // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
            // ConstExp → AddExp

            // initialize dimensions to symbol
            for (int i = 0 ; i < (array.isArray1() ? 1 : 2) ; i ++) {
                Node ConstExp = node.getSubNodesByType(Node.NodeType.ConstExp).get(i);
                int res = Calculator.calConstExp(ConstExp);
                array.dimensions.add(res);
            }

            // initialize values to symbol
            if (node.subNodesContain(Node.NodeType.ConstInitVal)) {
                Node InitVal = node.getSubNodesByType(Node.NodeType.ConstInitVal).get(0);
                for (int i = 0 ; i < InitVal.getSubNodesByType(Node.NodeType.ConstInitVal).size() ; i ++) {
                    Node curInitVal = InitVal.getSubNodesByType(Node.NodeType.ConstInitVal).get(i);
                    if (array.isArray1()) {
                        if (array.isGlobal) {
                            array.initValues.add(new Number(Calculator.calConstExp(curInitVal.getSubNodesByType(Node.NodeType.ConstExp).get(0))));
                        } else {
                            array.initValues.add(visitConstInitVal(curInitVal));
                        }
                    }
                    if (array.isArray2()) {
                        for (int j = 0 ; j < curInitVal.getSubNodesByType(Node.NodeType.ConstInitVal).size() ; j ++) {
                            Node subInitVal = curInitVal.getSubNodesByType(Node.NodeType.ConstInitVal).get(j);
                            if (array.isGlobal) {
                                array.initValues.add(new Number(Calculator.calConstExp(subInitVal.getSubNodesByType(Node.NodeType.ConstExp).get(0))));
                            } else {
                                array.initValues.add(visitConstInitVal(subInitVal));
                            }
                        }
                    }
                }
            }
        }
        if (node.type == Node.NodeType.VarDef) {
            // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
            // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
            // Exp → AddExp

            // initialize dimensions to symbol
            for (int i = 0 ; i < (array.isArray1() ? 1 : 2) ; i ++) {
                Node ConstExp = node.getSubNodesByType(Node.NodeType.ConstExp).get(i);
                int res = Calculator.calConstExp(ConstExp);
                array.dimensions.add(res);
            }

            // initialize values to symbol
            if (node.subNodesContain(Node.NodeType.InitVal)) {
                Node InitVal = node.getSubNodesByType(Node.NodeType.InitVal).get(0);
                for (int i = 0 ; i < InitVal.getSubNodesByType(Node.NodeType.InitVal).size() ; i ++) {
                    Node curInitVal = InitVal.getSubNodesByType(Node.NodeType.InitVal).get(i);
                    if (array.isArray1()) {
                        if (array.isGlobal) {
                            array.initValues.add(new Number(Calculator.calExp(curInitVal.getSubNodesByType(Node.NodeType.Exp).get(0))));
                        } else {
                            array.initValues.add(visitInitVal(curInitVal));
                        }
                    }
                    if (array.isArray2()) {
                        for (int j = 0 ; j < curInitVal.getSubNodesByType(Node.NodeType.InitVal).size() ; j ++) {
                            Node subInitVal = curInitVal.getSubNodesByType(Node.NodeType.InitVal).get(j);
                            if (array.isGlobal) {
                                array.initValues.add(new Number(Calculator.calExp(subInitVal.getSubNodesByType(Node.NodeType.Exp).get(0))));
                            } else {
                                array.initValues.add(visitInitVal(subInitVal));
                            }
                        }
                    }
                }
            }
        }

        if (array.isGlobal) {
            return;
        }

        // initialize values to ir
        List<Operand> index = new ArrayList<>();
        for (int i = 0 ; i < array.dimensions.size() ; i ++) {
            index.add(new Number(0));
        }
        for (int i = 0 ; i < array.initValues.size() ; i ++) {
            index.add(new Number(i));
            Symbol tmp = tempCounter.getPointer(level, array, index);
            Operand value = array.initValues.get(i);
            curBasicBlock.addToChain(new ElementPtr(tmp, array, index));
            curBasicBlock.addToChain(new PointerOp(PointerOp.Op.STORE, tmp, value));
            index.remove(index.size() -1);
        }
    }

    private Operand visitInitVal(Node node) {
        assert node.subNodesContain(Node.NodeType.Exp) : "visitInitVal error";
        Node Exp = node.getSubNodesByType(Node.NodeType.Exp).get(0);
        return visitExp(Exp);
    }

    private Operand visitConstInitVal(Node node) {
        assert node.subNodesContain(Node.NodeType.ConstExp) : "visitConstInitVal error";
        Node ConstExp = node.getSubNodesByType(Node.NodeType.ConstExp).get(0);
        return visitConstExp(ConstExp);
    }

    public Operand visitConstExp(Node node) {
        // ConstExp → AddExp
        return visitAddExp(node.nodeList.get(0));
    }

    private void visitStmt(Node node) {
        //  Stmt → LVal '=' Exp ';'
        //       | [Exp] ';'
        //       | Block
        //       | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        //       | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        //       | 'break' ';'
        //       | 'continue' ';'
        //       | 'return' [Exp] ';'
        //       | LVal '=' 'getint''('')'';'
        //       | 'printf''('FormatString{','Exp}')'';'
        if (node.nodeList.get(0).type == Node.NodeType.RETURNTK) {  // 'return' [Exp] ';'
            visitReturn(node);
        } else if (node.nodeList.get(0).type == Node.NodeType.PRINTFTK) {  // 'printf''('FormatString{','Exp}')'';'
            visitPrintf(node);
        } else if (node.subNodesContain(Node.NodeType.GETINTTK)) {  // LVal '=' 'getint''('')'';'
            visitAssign(node);
        } else if (node.subNodesContain(Node.NodeType.ASSIGN)) {  // LVal '=' Exp ';'
            visitAssign(node);
        } else if (node.subNodesContain(Node.NodeType.Block)) {  // Block
            visitBlock(node.getSubNodesByType(Node.NodeType.Block).get(0));
        } else if (node.nodeList.get(0).type == Node.NodeType.Exp) {  // [Exp] ';'
            visitExp(node.getSubNodesByType(Node.NodeType.Exp).get(0));
        } else if (node.subNodesContain(Node.NodeType.IFTK)) {  // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            visitBranch(node);
        } else if (node.subNodesContain(Node.NodeType.FORTK)) {  // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            visitLoop(node);
        } else if (node.subNodesContain(Node.NodeType.BREAKTK)) {  // 'break' ';'
            visitBreak(node);
        } else if (node.subNodesContain(Node.NodeType.CONTINUETK)) {  // 'continue' ';'
            visitContinue(node);
        }
    }

    private void visitBreak(Node node) {
        curBasicBlock.addToChain(new Jump(label5));
    }

    private void visitContinue(Node node) {
        curBasicBlock.addToChain(new Jump(label4));
    }

    private void visitLoop(Node node) {
        String oldLabel1 = label1;
        String oldLabel2 = label2;
        String oldLabel3 = label3;
        String oldLabel4 = label4;
        String oldLabel5 = label5;

        Node ForStmt1 = node.nodeList.get(2).type == Node.NodeType.ForStmt ? node.nodeList.get(2) : null;
        Node Cond = node.subNodesContain(Node.NodeType.Cond) ? node.getSubNodesByType(Node.NodeType.Cond).get(0) : null;
        Node ForStmt2 = getForStmt2(node, ForStmt1, Cond);
        Node Stmt = node.getSubNodesByType(Node.NodeType.Stmt).get(0);

        label1 = labelCounter.getLabel("for");
        label2 = labelCounter.getLabel("for");
        label3 = labelCounter.getLabel("for");
        label4 = labelCounter.getLabel("for");
        label5 = labelCounter.getLabel("for");

        // ForStmt1 label1
        createBasicBlock(label1);
        if (ForStmt1 == null) {
            curBasicBlock.addToChain(new Jump(label2));
        } else {
            visitForStmt(ForStmt1);
        }

        // Cond label2
        createBasicBlock(label2);
        if (Cond == null) {
            curBasicBlock.addToChain(new Jump(label3));
        } else {
            Operand _condition = visitCond(Cond);
            Operand condition = tempCounter.getTemp(level);
            if (curBasicBlock.getLastInstruction() instanceof BinaryOperator && ((BinaryOperator) curBasicBlock.getLastInstruction()).isI1()) {
                curBasicBlock.addToChain(new Trunc(_condition, condition, "i1"));
            } else {
                curBasicBlock.addToChain(new Trunc(_condition, condition, "i32"));
            }
            curBasicBlock.addToChain(new Branch(condition, label3, label5));
        }

        // Stmt label3
        createBasicBlock(label3);
        visitStmt(Stmt);
        curBasicBlock.addToChain(new Jump(label4));

        // ForStmt2 label4
        createBasicBlock(label4);
        if (ForStmt2 == null) {
            curBasicBlock.addToChain(new Jump(label2));
        } else {
            visitForStmt(ForStmt2);
            curBasicBlock.addToChain(new Jump(label2));
        }

        // end label5
        createBasicBlock(label5);

        label1 = oldLabel1;
        label2 = oldLabel2;
        label3 = oldLabel3;
        label4 = oldLabel4;
        label5 = oldLabel5;
    }

    private void visitForStmt(Node node) {
        // ForStmt → LVal '=' Exp
        visitAssign(node);
    }

    private void visitBranch(Node node) {
        Operand _condition = visitCond(node.getSubNodesByType(Node.NodeType.Cond).get(0));
        Operand condition = tempCounter.getTemp(level);
        if (curBasicBlock.getLastInstruction() instanceof BinaryOperator && ((BinaryOperator) curBasicBlock.getLastInstruction()).isI1() || curBasicBlock.getLastInstruction() instanceof UnaryOperator && ((UnaryOperator) curBasicBlock.getLastInstruction()).isI1()) {
            curBasicBlock.addToChain(new Trunc(_condition, condition, "i1"));
        } else {
            curBasicBlock.addToChain(new Trunc(_condition, condition, "i32"));
        }
        String afterLabel = labelCounter.getLabel();

        if (node.nodeList.size() == 5) {  // without else statement
            String ifLabel = labelCounter.getLabel("if");
            curBasicBlock.addToChain(new Branch(condition, ifLabel, afterLabel, Branch.Op.BEQ));
            createBasicBlock(ifLabel);
            curBranchLabel = ifLabel;
            visitStmt(node.getSubNodesByType(Node.NodeType.Stmt).get(0));
        } else {  // with else statement
            String ifLabel = labelCounter.getLabel("if");
            String elseLabel = labelCounter.getLabel("else");
            curBasicBlock.addToChain(new Branch(condition, ifLabel, elseLabel, Branch.Op.BNE));
            createBasicBlock(elseLabel);
            curBranchLabel =elseLabel;
            visitStmt(node.getSubNodesByType(Node.NodeType.Stmt).get(1));
            curBasicBlock.addToChain(new Jump(afterLabel));
            createBasicBlock(ifLabel);
            curBranchLabel = ifLabel;
            visitStmt(node.getSubNodesByType(Node.NodeType.Stmt).get(0));
        }

        createBasicBlock(afterLabel);
    }

    private void createBasicBlock(String label) {
        curBasicBlock = new BasicBlock(label);
        curFuncDef.addBasicBlock(curBasicBlock);
    }

    private Operand visitCond(Node node) {
        // Cond → LOrExp
        return visitLOrExp(node.getSubNodesByType(Node.NodeType.LOrExp).get(0));
    }

    private Operand visitLOrExp(Node node) {
        // LOrExp → LAndExp | LOrExp '||' LAndExp
        if (node.nodeList.size() == 1) {
            return visitLAndExp(node.getSubNodesByType(Node.NodeType.LAndExp).get(0));
        } else {
            assert false : "in visitLOrExp";
        }
        return null;
    }

    private Operand visitLAndExp(Node node) {
        // LAndExp → EqExp | LAndExp '&&' EqExp
        if (node.nodeList.size() == 1) {
            return visitEqExp(node.getSubNodesByType(Node.NodeType.EqExp).get(0));
        } else {
            assert false : "in visitLAndExp";
        }
        return null;
    }

    private Operand visitEqExp(Node node) {
        // EqExp → RelExp | EqExp ('==' | '!=') RelExp
        if (node.nodeList.size() == 1) {
            return visitRelExp(node.getSubNodesByType(Node.NodeType.RelExp).get(0));
        } else {
            Operand left = visitEqExp(node.getSubNodesByType(Node.NodeType.EqExp).get(0));
            Operand right = visitRelExp(node.getSubNodesByType(Node.NodeType.RelExp).get(0));
            Symbol tmp = tempCounter.getTemp(level);
            Symbol tmp2 = tempCounter.getTemp(level);
            if (node.subNodesContain(Node.NodeType.EQL)) {
                curBasicBlock.addToChain(new BinaryOperator(left, right, BinaryOperator.Op.EQL, tmp));
                curBasicBlock.addToChain(new Zext(tmp, tmp2));
            } else {
                curBasicBlock.addToChain(new BinaryOperator(left, right, BinaryOperator.Op.NEQ, tmp));
                curBasicBlock.addToChain(new Zext(tmp, tmp2));
            }
            return tmp2;
        }
    }

    private Operand visitRelExp(Node node) {
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        if (node.nodeList.size() == 1) {
            return visitAddExp(node.getSubNodesByType(Node.NodeType.AddExp).get(0));
        } else {
            Operand left = visitRelExp(node.getSubNodesByType(Node.NodeType.RelExp).get(0));
            Operand right = visitAddExp(node.getSubNodesByType(Node.NodeType.AddExp).get(0));
            Symbol tmp = tempCounter.getTemp(level);
            Symbol tmp2 = tempCounter.getTemp(level);
            if (node.subNodesContain(Node.NodeType.LSS)) {  // <
                curBasicBlock.addToChain(new BinaryOperator(left, right, BinaryOperator.Op.LSS, tmp));
                curBasicBlock.addToChain(new Zext(tmp, tmp2));
            } else if (node.subNodesContain(Node.NodeType.GRE)) {  // >
                curBasicBlock.addToChain(new BinaryOperator(left, right, BinaryOperator.Op.GRE, tmp));
                curBasicBlock.addToChain(new Zext(tmp, tmp2));
            } else if (node.subNodesContain(Node.NodeType.LEQ)) {  // <=
                curBasicBlock.addToChain(new BinaryOperator(left, right, BinaryOperator.Op.LEQ, tmp));
                curBasicBlock.addToChain(new Zext(tmp, tmp2));
            } else {  // >=
                curBasicBlock.addToChain(new BinaryOperator(left, right, BinaryOperator.Op.GEQ, tmp));
                curBasicBlock.addToChain(new Zext(tmp, tmp2));
            }
            return tmp2;
        }
    }

    private void visitAssign(Node node) {
        Node left = node.getSubNodesByType(Node.NodeType.LVal).get(0);
        Symbol dst = node.getSymbolTable().getVisableSymbol(left.nodeList.get(0).name);
        Operand value;
        if (node.subNodesContain(Node.NodeType.GETINTTK)) {  // getint
            value = visitGetInt();
            if (dst.isArrayOrPointer()) {
                dst = getElementPointer(dst, left);
            }
        } else {  // assign
            value = visitExp(node.getSubNodesByType(Node.NodeType.Exp).get(0));
            if (dst.isArrayOrPointer()) {
                dst = getElementPointer(dst, left);
            }
        }
        curBasicBlock.addToChain(new PointerOp(PointerOp.Op.STORE, dst, value));
    }

    private Symbol getElementPointer(Symbol base, Node node) {
        List<Operand> index = new ArrayList<>();
        if (base.isArray()) {
            index.add(new Number(0));
        }
        for (Node exp : node.getSubNodesByType(Node.NodeType.Exp)) {
            index.add(visitExp(exp));
        }
        if (isCalling && !node.subNodesContain(Node.NodeType.LBRACK) || base.isArray2() && node.getSubNodesByType(Node.NodeType.LBRACK).size() == 1) {
            index.add(new Number(-1));
        } else if (base.isPointer() && base.dimensions.size() == 2 && node.getSubNodesByType(Node.NodeType.LBRACK).size() == 1) {
            index.add(new Number(0));
        }
        Symbol tmp = tempCounter.getPointer(level, base, index);
        if (isCalling && !node.subNodesContain(Node.NodeType.LBRACK) || base.isArray2() && node.getSubNodesByType(Node.NodeType.LBRACK).size() == 1) {
            index.remove(index.size()-1);
            index.add((new Number(0)));
        }
        curBasicBlock.addToChain(new ElementPtr(tmp, base, index));
        return tmp;
    }

    private Operand visitGetInt() {
        Symbol dst = tempCounter.getTemp(level);
        curBasicBlock.addToChain(new Input(dst));
        return dst;
    }

    private void visitPrintf(Node node) {
        String formatString = node.getSubNodesByType(Node.NodeType.STRCON).get(0).name;
        formatString = formatString.substring(1, formatString.length()-1);
        List<String> formatStrings = Arrays.stream((formatString.split("(?<=%d)|(?=%d)"))).toList();
        List<Node> args = node.getSubNodesByType(Node.NodeType.Exp);
        List<Operand> operands = new ArrayList<>();
        for (Node arg : args) {
            operands.add(visitExp(arg));
        }
        int cnt = 0;
        for (String string : formatStrings) {
            if (string.equals("%d")) {
                Operand operand = operands.get(cnt++);
                curBasicBlock.addToChain(new PrintInt(operand));
            } else {
                curBasicBlock.addToChain(new PrintStr(string));
            }
        }
    }

    private void visitReturn(Node node) {
        // 'return' [Exp] ';'
        if (node.nodeList.size() == 2) {
            this.curBasicBlock.addToChain(new Return(null));
        } else {
            Operand returnValue = visitExp(node.nodeList.get(1));
            this.curBasicBlock.addToChain(new Return(returnValue));
        }
    }

    public Operand visitExp(Node node) {
        //  Exp → AddExp
        return visitAddExp(node.nodeList.get(0));
    }

    private Operand visitAddExp(Node node) {
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        if (node.nodeList.size() == 1) {
            return visitMulExp(node.nodeList.get(0));
        } else {
            Operand left = visitAddExp(node.nodeList.get(0));
            Operand right = visitMulExp(node.nodeList.get(2));
            Symbol res = tempCounter.getTemp(level);
            curBasicBlock.addToChain(new BinaryOperator(left, right, BinaryOperator.node2Op.get(node.nodeList.get(1).type), res));
            return res;
        }
    }

    private Operand visitMulExp(Node node) {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        if (node.nodeList.size() == 1) {
            return visitUnaryExp(node.nodeList.get(0));
        } else {
            Operand left = visitMulExp(node.nodeList.get(0));
            Operand right = visitUnaryExp(node.nodeList.get(2));
            Symbol res = tempCounter.getTemp(level);
            curBasicBlock.addToChain(new BinaryOperator(left, right, BinaryOperator.node2Op.get(node.nodeList.get(1).type), res));
            return res;
        }
    }

    private Operand visitUnaryExp(Node node) {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (node.nodeList.size() == 1) {  // PrimaryExp
            return visitPrimaryExp(node.nodeList.get(0));
        } else if (node.nodeList.size() == 2) {  // UnaryOp UnaryExp
            Node unaryOp = node.nodeList.get(0);
            Node unaryExp = node.nodeList.get(1);
            if (unaryOp.nodeList.get(0).type == Node.NodeType.PLUS) {
                return visitUnaryExp(unaryExp);
            } else {
                Operand right = visitUnaryExp(unaryExp);
                Symbol res = tempCounter.getTemp(level);
                curBasicBlock.addToChain(new UnaryOperator(UnaryOperator.node2Op.get(unaryOp.nodeList.get(0).type), res, right));
                if (((UnaryOperator) curBasicBlock.getLastInstruction()).isI1()) {
                    Symbol res2 = tempCounter.getTemp(level);
                    curBasicBlock.addToChain(new Zext(res, res2));
                    return res2;
                } else {
                    return res;
                }
            }
        } else {  // Ident '(' [FuncRParams] ')'
            Symbol func = findSymbol(node.nodeList.get(0).name);
            Symbol dst = func.symbolValueType == Symbol.ValueType.INT ? tempCounter.getTemp(level) : null;
            List<Operand> args = getFuncCallParams(node);
            curBasicBlock.addToChain(new Call(func, args, dst));
            return dst;
        }
    }

    private Operand visitPrimaryExp(Node node) {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        if (node.nodeList.get(0).type == Node.NodeType.Number) {  // Number
            return visitNumber(node.nodeList.get(0));
        } else if (node.nodeList.get(0).type == Node.NodeType.LPARENT) {  // '(' Exp ')'
            return visitExp(node.nodeList.get(1));
        } else {  // LVal
            return visitLVal(node.nodeList.get(0));
        }
    }

    private Operand visitLVal(Node node) {
        // LVal → Ident {'[' Exp ']'}
        Symbol dst = null;
        Symbol src = node.getSymbolTable().getVisableSymbol(node.nodeList.get(0).name);
        if (src.isVar()) {  // VAR
            if (curFuncDef.isParam(node.nodeList.get(0).name)) {
//                Symbol tmp = tempCounter.getTemp(level);
//                curBasicBlock.addToChain(new VarDef(tmp));
//                curBasicBlock.addToChain(new PointerOp(PointerOp.Op.STORE, tmp, src));
                dst = tempCounter.getTemp(level);
                curBasicBlock.addToChain(new PointerOp(PointerOp.Op.LOAD, dst, src));
            } else {
                dst = tempCounter.getTemp(level);
                curBasicBlock.addToChain(new PointerOp(PointerOp.Op.LOAD, dst, src));
            }
        }
        if (src.isArrayOrPointer()) {  // ARRAY
            Symbol tmp = getElementPointer(src, node);
            if (src.dimensions.size() - node.getSubNodesByType(Node.NodeType.Exp).size() == 0) {
                dst = tempCounter.getTemp(level);
                curBasicBlock.addToChain(new PointerOp(PointerOp.Op.LOAD, dst, tmp));
            } else {
                dst = tmp;
            }
        }
        return dst;
    }

    private Operand visitNumber(Node node) {
        // Number → IntCon
        return visitIntCon(node.nodeList.get(0));
    }

    private Operand visitIntCon(Node node) {
        return new Number(Integer.parseInt(node.name));
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    // below are helper functions
    private Symbol findSymbol(String symbolName) {
        return this.rootSymbolTable.getVisableSymbol(symbolName);
    }

    private void setFuncFParams(Node funcFParams) {
        List<Node> funcFParamList = funcFParams.getSubNodesByType(Node.NodeType.FuncFParam);
        List<Symbol> params = new ArrayList<>();
        for (Node funcFParam : funcFParamList) {
//            isCalling = true;
            String ident = funcFParam.getSubNodesByType(Node.NodeType.IDENFR).get(0).name;
            funcFParams.getSymbolTable().setSymbolDefined(ident);
            Symbol symbol = funcFParams.getSymbolTable().getVisableSymbol(ident);
            symbol.isGlobal = false;
            symbol.level ++;
            symbol.isParam = true;
//            symbol.dimensions.add(0);
            if (funcFParam.getSubNodesByType(Node.NodeType.LBRACK).size() == 1) {
                symbol.dimensions.add(0);
                symbol.symbolVarType = Symbol.VarType.POINTER;
            }
            if (funcFParam.getSubNodesByType(Node.NodeType.LBRACK).size() == 2) {
                symbol.dimensions.add(0);
                int value = Calculator.calConstExp(funcFParam.getSubNodesByType(Node.NodeType.ConstExp).get(0));
                symbol.dimensions.add(value);
                symbol.symbolVarType = Symbol.VarType.POINTER;
            }
            params.add(symbol);
//            isCalling = false;
        }
        this.curFuncDef.symbol.params = params;
    }

    private List<Operand> getFuncCallParams(Node node) {
        List<Operand> ret = new ArrayList<>();
        if (node.getSubNodesByType(Node.NodeType.FuncRParams).size() == 0) {
            return ret;
        }
        Node funcRParams = node.getSubNodesByType(Node.NodeType.FuncRParams).get(0);
        List<Node> exps = funcRParams.getSubNodesByType(Node.NodeType.Exp);
        for (Node exp : exps) {
            isCalling = true;
            ret.add(visitExp(exp));
            isCalling = false;
        }
        return ret;
    }

    public static void refactorBranchOr(Node node) {
        if (node != null && node.type == Node.NodeType.Stmt && node.nodeList != null && node.subNodesContain(Node.NodeType.IFTK)) {
            if (toBeRefactoredOr(node)) {
                branchReducedOr = false;

                int line = node.nodeLine;
                SymbolTable symbolTable = node.getSymbolTable();
                Node LOr = node.getSubNodesByType(Node.NodeType.Cond).get(0).getSubNodesByType(Node.NodeType.LOrExp).get(0).getSubNodesByType(Node.NodeType.LOrExp).get(0);
                Node LAnd = node.getSubNodesByType(Node.NodeType.Cond).get(0).getSubNodesByType(Node.NodeType.LOrExp).get(0).getSubNodesByType(Node.NodeType.LAndExp).get(0);
                Node Stmt1 = node.getSubNodesByType(Node.NodeType.Stmt).get(0);
                Node Stmt2 = node.getSubNodesByType(Node.NodeType.Stmt).size() == 1 ? null : node.getSubNodesByType(Node.NodeType.Stmt).get(1);

                List<Node> newCondSub = new ArrayList<>();
                newCondSub.add(LOr);
                Node newCond = new Node(line, "Cond", newCondSub, Node.NodeType.Cond, symbolTable);

                Node oldCond = node.getSubNodesByType(Node.NodeType.Cond).get(0);
                oldCond.getSubNodesByType(Node.NodeType.LOrExp).get(0).nodeList.remove(0);  // remove LOr
                oldCond.getSubNodesByType(Node.NodeType.LOrExp).get(0).nodeList.remove(0);  // remove '||'

                List<Node> newStmtSub = new ArrayList<>();
                newStmtSub.add(new Node(line, "if", null, Node.NodeType.IFTK, symbolTable));
                newStmtSub.add(new Node(line, "(", null, Node.NodeType.LPARENT, symbolTable));
                newStmtSub.add(oldCond);
                newStmtSub.add(new Node(line, ")", null, Node.NodeType.RPARENT, symbolTable));
                newStmtSub.add(deepCopy(Stmt1));
                if(Stmt2 != null) {
                    newStmtSub.add(new Node(line, "else", null, Node.NodeType.ELSETK, symbolTable));
                    newStmtSub.add(Stmt2);
                }
                Node newStmt = new Node(line, "Stmt", newStmtSub, Node.NodeType.Stmt, symbolTable);

                node.nodeList.remove(2);
                node.nodeList.add(2, newCond);
                if (Stmt2 != null) {
                    node.nodeList.remove(6);
                    node.nodeList.add(newStmt);
                } else {
                    node.nodeList.add(new Node(line, "else", null, Node.NodeType.ELSETK, symbolTable));
                    node.nodeList.add(newStmt);
                }
            }
        }

        if (node != null && node.nodeList != null) {
            for (Node n : node.nodeList) {
                refactorBranchOr(n);
            }
        }
    }

    private static boolean toBeRefactoredOr(Node stmt) {
        Node LOr = stmt.getSubNodesByType(Node.NodeType.Cond).get(0).getSubNodesByType(Node.NodeType.LOrExp).get(0);
        return LOr.nodeList.size() != 1;
    }

    public static void refactorBranchAnd(Node node) {
        if (node != null && node.type == Node.NodeType.Stmt && node.nodeList != null && node.subNodesContain(Node.NodeType.IFTK)) {
            if (toBeRefactoredAnd(node)) {
                branchReducedAnd = false;

                int line = node.nodeLine;
                SymbolTable symbolTable = node.getSymbolTable();
                Node LAnd = node.getSubNodesByType(Node.NodeType.Cond).get(0).getSubNodesByType(Node.NodeType.LOrExp).get(0).getSubNodesByType(Node.NodeType.LAndExp).get(0).getSubNodesByType(Node.NodeType.LAndExp).get(0);
                Node Eq = node.getSubNodesByType(Node.NodeType.Cond).get(0).getSubNodesByType(Node.NodeType.LOrExp).get(0).getSubNodesByType(Node.NodeType.LAndExp).get(0).getSubNodesByType(Node.NodeType.EqExp).get(0);
                Node Stmt1 = node.getSubNodesByType(Node.NodeType.Stmt).get(0);
                Node Stmt2 = node.getSubNodesByType(Node.NodeType.Stmt).size() == 1 ? null : node.getSubNodesByType(Node.NodeType.Stmt).get(1);

                Node LOr1 = node.getSubNodesByType(Node.NodeType.Cond).get(0).getSubNodesByType(Node.NodeType.LOrExp).get(0);
                LOr1.nodeList.remove(0);
                LOr1.nodeList.add(LAnd);

                List<Node> newLandSub = new ArrayList<>();
                newLandSub.add(Eq);
                Node newLAnd = new Node(line, "LAnd", newLandSub, Node.NodeType.LAndExp, symbolTable);
                List<Node> newLOrSub = new ArrayList<>();
                newLOrSub.add(newLAnd);
                Node newLOr = new Node(line, "LOr", newLOrSub, Node.NodeType.LOrExp, symbolTable);
                List<Node> newCondSub = new ArrayList<>();
                newCondSub.add(newLOr);
                Node newCond = new Node(line, "Cond", newCondSub, Node.NodeType.Cond, symbolTable);
                List<Node> newStmtSub = new ArrayList<>();
                newStmtSub.add(new Node(line, "if", null, Node.NodeType.IFTK, symbolTable));
                newStmtSub.add(new Node(line, "(", null, Node.NodeType.LPARENT, symbolTable));
                newStmtSub.add(newCond);
                newStmtSub.add(new Node(line, ")", null, Node.NodeType.RPARENT, symbolTable));
                newStmtSub.add(Stmt1);
                if (Stmt2 != null) {
                    newStmtSub.add(new Node(line, "else", null, Node.NodeType.ELSETK, symbolTable));
                    newStmtSub.add(deepCopy(Stmt2));
                }
                Node newStmt = new Node(line, "Stmt", newStmtSub, Node.NodeType.Stmt, symbolTable);
                node.nodeList.remove(4);
                node.nodeList.add(4, newStmt);
            }
        }

        if (node != null && node.nodeList != null) {
            for (Node n : node.nodeList) {
                refactorBranchAnd(n);
            }
        }
    }

    private static Node deepCopy(Node node) {
        return node.copy();
    }

    private static boolean toBeRefactoredAnd(Node stmt) {
        Node LAnd = stmt.getSubNodesByType(Node.NodeType.Cond).get(0).getSubNodesByType(Node.NodeType.LOrExp).get(0).getSubNodesByType(Node.NodeType.LAndExp).get(0);
        return LAnd.nodeList.size() != 1;
    }

    public static Node getForStmt2(Node node, Node ForStmt1, Node Cond) {
        Node res = null;
        if (ForStmt1 == null) {
            res = node.subNodesContain(Node.NodeType.ForStmt) ? node.getSubNodesByType(Node.NodeType.ForStmt).get(0) : null;
        } else if(Cond == null) {
            res = node.nodeList.get(5).type == Node.NodeType.ForStmt ? node.nodeList.get(5) : null;
        } else {
            res = node.nodeList.get(6).type == Node.NodeType.ForStmt ? node.nodeList.get(6) : null;
        }
        assert res == null || res.type == Node.NodeType.ForStmt : "in getForStmt2";
        return res;
    }

    public static void refactorLoop(Node node) {
        if (node != null && node.type == Node.NodeType.Stmt && node.nodeList != null && node.subNodesContain(Node.NodeType.FORTK)) {
            int line = node.nodeLine;
            SymbolTable symbolTable = node.getSymbolTable();

            Node ForStmt1 = node.nodeList.get(2).type == Node.NodeType.ForStmt ? node.nodeList.get(2) : null;
            Node Cond = node.subNodesContain(Node.NodeType.Cond) ? node.getSubNodesByType(Node.NodeType.Cond).get(0) : null;
            Node ForStmt2 = getForStmt2(node, ForStmt1, Cond);
            Node Stmt = node.getSubNodesByType(Node.NodeType.Stmt).get(0);

            if (Cond == null) {
                if (node.nodeList != null) {
                    for (Node n : node.nodeList) {
                        refactorLoop(n);
                    }
                }
                return ;
            }

            List<Node> breakStmtSub = new ArrayList<>();
            breakStmtSub.add(new Node(line, "break", null, Node.NodeType.BREAKTK, symbolTable));
            breakStmtSub.add(new Node(line, ";", null, Node.NodeType.SEMICN, symbolTable));
            Node breakStmt = new Node(line, "Stmt", breakStmtSub, Node.NodeType.Stmt, symbolTable);
            List<Node> newStmtSub = new ArrayList<>();
            newStmtSub.add(new Node(line, "if", null, Node.NodeType.IFTK, symbolTable));
            newStmtSub.add(new Node(line, "(", null, Node.NodeType.LPARENT, symbolTable));
            newStmtSub.add(Cond);
            newStmtSub.add(new Node(line, ")", null, Node.NodeType.RPARENT, symbolTable));
            newStmtSub.add(Stmt);
            newStmtSub.add(new Node(line, "else", null, Node.NodeType.ELSETK, symbolTable));
            newStmtSub.add(breakStmt);
            Node newStmt = new Node(line, "Stmt", newStmtSub, Node.NodeType.Stmt, symbolTable);

            Node newIntConst = new Node(line, "1", null, Node.NodeType.INTCON, symbolTable);

            List<Node> newNumberSub = new ArrayList<>();
            newNumberSub.add(newIntConst);
            Node newNumber = new Node(line, "Number", newNumberSub, Node.NodeType.Number, symbolTable);

            List<Node> newPrimaryExpSub = new ArrayList<>();
            newPrimaryExpSub.add(newNumber);
            Node newPrimaryExp = new Node(line, "PrimaryExp", newPrimaryExpSub, Node.NodeType.PrimaryExp, symbolTable);

            List<Node> newUnaryExpSub = new ArrayList<>();
            newUnaryExpSub.add(newPrimaryExp);
            Node newUnaryExp = new Node(line, "UnaryExp", newUnaryExpSub, Node.NodeType.UnaryExp, symbolTable);

            List<Node> newMulExpSub = new ArrayList<>();
            newMulExpSub.add(newUnaryExp);
            Node newMulExp = new Node(line, "MulExp", newMulExpSub, Node.NodeType.MulExp, symbolTable);

            List<Node> newAddExpSub = new ArrayList<>();
            newAddExpSub.add(newMulExp);
            Node newAddExp = new Node(line, "AddExp", newAddExpSub, Node.NodeType.AddExp, symbolTable);

            List<Node> newRelExpSub = new ArrayList<>();
            newRelExpSub.add(newAddExp);
            Node newRelExp = new Node(line, "RelExp", newRelExpSub, Node.NodeType.RelExp, symbolTable);

            List<Node> newEqExpSub = new ArrayList<>();
            newEqExpSub.add(newRelExp);
            Node newEqExp = new Node(line, "EqExp", newEqExpSub, Node.NodeType.EqExp, symbolTable);

            List<Node> newLAndExpSub = new ArrayList<>();
            newLAndExpSub.add(newEqExp);
            Node newLAndExp = new Node(line, "LAndExp", newLAndExpSub, Node.NodeType.LAndExp, symbolTable);

            List<Node> newLOrExpSub = new ArrayList<>();
            newLOrExpSub.add(newLAndExp);
            Node newLOrExp = new Node(line, "LOrExp", newLOrExpSub, Node.NodeType.LOrExp, symbolTable);

            List<Node> newCondSub = new ArrayList<>();
            newCondSub.add(newLOrExp);
            Node newCond = new Node(line, "Cond", newCondSub, Node.NodeType.Cond, symbolTable);

            List<Node> newList = new ArrayList<>();
            newList.add(new Node(line, "for", null, Node.NodeType.FORTK, symbolTable));
            newList.add(new Node(line, "(", null, Node.NodeType.LPARENT, symbolTable));
            if (ForStmt1 != null) {
                newList.add(ForStmt1);
            }
            newList.add(new Node(line, ";", null, Node.NodeType.SEMICN, symbolTable));
            newList.add(newCond);
            newList.add(new Node(line, ";", null, Node.NodeType.SEMICN, symbolTable));
            if (ForStmt2 != null) {
                newList.add(ForStmt2);
            }
            newList.add(new Node(line, ")", null, Node.NodeType.RPARENT, symbolTable));
            newList.add(newStmt);
            node.nodeList =newList;
        }

        if (node != null && node.nodeList != null) {
            for (Node n : node.nodeList) {
                refactorLoop(n);
            }
        }
    }

}
