package mid.ir;

import frontend.Node;
import frontend.Parser;
import frontend.Symbol;

import java.util.HashMap;
import java.util.Map;

public class UnaryOperator extends Instruction {

    public enum Op {
        PLUS, MINU, NOT
    }

    public static final Map<Node.NodeType, Op> node2Op = new HashMap<>() {
        {
            put(Node.NodeType.PLUS, Op.PLUS);
            put(Node.NodeType.MINU, Op.MINU);
            put(Node.NodeType.NOT, Op.NOT);
        }
    };

    private static final Map<Op, String> op2Ir = new HashMap<Op, String>() {
        {
            put(Op.PLUS, "add");
            put(Op.MINU, "sub");
            put(Op.NOT, "eq");
        }
    };

    private final Op operator;
    private final Symbol res;
    private final Operand right;

    public UnaryOperator(Op operator, Symbol res, Operand right) {
        this.operator = operator;
        this.res = res;
        this.right = right;
    }

    @Override
    public String toIrString() {
        return switch (operator) {
            case PLUS, MINU -> "\t" + res.value2Ir() + " = " + op2Ir.get(operator) + " "
                    + Symbol.type2Ir.get(res.symbolValueType) + " " +
                    "0, " +
                    right.value2Ir();
            case NOT -> "\t" + res.value2Ir() + " = icmp " + op2Ir.get(operator) + " "
                    + Symbol.type2Ir.get(res.symbolValueType) + " " +
                    "0, " +
                    right.value2Ir();
        };
    }

    public boolean isI1() {
        return this.operator == Op.NOT;
    }

    public Op getOperator() {
        return operator;
    }

    public Symbol getRes() {
        return res;
    }

    public Operand getRight() {
        return right;
    }

}
