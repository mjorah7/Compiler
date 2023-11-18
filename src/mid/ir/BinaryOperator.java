package mid.ir;

import frontend.Node;
import frontend.Symbol;
import mid.IrGenerator;

import javax.swing.plaf.multi.MultiOptionPaneUI;
import java.util.HashMap;
import java.util.Map;

public class BinaryOperator extends Instruction {

    public enum Op {
        ADD, SUB, MULT, DIV, MOD, EQL, GEQ, GRE, LEQ, LSS, NEQ
    }

    public static final Map<Node.NodeType, Op> node2Op = new HashMap<>() {
        {
            put(Node.NodeType.PLUS, Op.ADD);
            put(Node.NodeType.MINU, Op.SUB);
            put(Node.NodeType.MULT, Op.MULT);
            put(Node.NodeType.DIV, Op.DIV);
            put(Node.NodeType.MOD, Op.MOD);
            put(Node.NodeType.EQL, Op.EQL);
            put(Node.NodeType.NEQ, Op.NEQ);
            put(Node.NodeType.GRE, Op.GRE);
            put(Node.NodeType.GEQ, Op.GEQ);
            put(Node.NodeType.LEQ, Op.LEQ);
            put(Node.NodeType.LSS, Op.LSS);
        }
    };

    private static final Map<Op, String> op2Ir = new HashMap<>() {
        {
            put(Op.ADD, "add");
            put(Op.SUB, "sub");
            put(Op.MULT, "mul");
            put(Op.DIV, "sdiv");
            put(Op.MOD, "srem");
            put(Op.EQL, "eq");
            put(Op.NEQ, "ne");
            put(Op.GRE, "sgt");
            put(Op.GEQ, "sge");
            put(Op.LEQ, "sle");
            put(Op.LSS, "slt");
        }
    };

    private final Operand left;
    private final Operand right;
    private final Op operator;
    private final Symbol res;

    public BinaryOperator(Operand left, Operand right, Op operator, Symbol res) {
        this.left = left;
        this.right = right;
        this.operator = operator;
        this.res = res;
    }

    @Override
    public String toIrString() {
        return switch (operator) {
            case ADD, SUB, MULT, DIV, MOD -> table() + res.value2Ir() + " = " + op2Ir.get(operator) + " "
                    + Symbol.type2Ir.get(res.symbolValueType) + " " +
                    left.value2Ir() + ", " +
                    right.value2Ir();
            case NEQ, LSS, LEQ, GRE, GEQ, EQL -> table() + res.value2Ir() + " = icmp " + op2Ir.get(operator) + " "
                    + Symbol.type2Ir.get(res.symbolValueType) + " " +
                    left.value2Ir() + ", " +
                    right.value2Ir();
        };
    }

    public boolean isI1() {
        // EQL, GEQ, GRE, LEQ, LSS, NEQ
        return this.operator == Op.EQL || this.operator == Op.GEQ || this.operator == Op.GRE ||
                this.operator == Op.LEQ || this.operator == Op.LSS || this.operator == Op.NEQ;
    }

}
