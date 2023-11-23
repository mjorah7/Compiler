package mid.ir;

public class Branch extends Instruction {

    public enum Op {
        BEQ, BNE
    }

    private final Operand condition;
    private final String labelTrue;
    private final String labelFalse;
    private final Op operator;

    public Branch(Operand condition, String labelTrue, String labelFalse, Op operator) {
        super();
        this.condition = condition;
        this.labelTrue = labelTrue;
        this.labelFalse = labelFalse;
        this.operator = operator;
    }

    public Branch(Operand condition, String labelTrue, String labelFalse) {
        super();
        this.condition = condition;
        this.labelTrue = labelTrue;
        this.labelFalse = labelFalse;
        this.operator = Op.BNE;
    }

    @Override
    public String toIrString() {
        return table() + "br i1 " + condition.value2Ir() + ", label %" + labelTrue + ", label %" + labelFalse;
    }

    public Operand getCondition() {
        return condition;
    }

    public String getLabelTrue() {
        return labelTrue;
    }

    public String getLabelFalse() {
        return labelFalse;
    }

    public Op getOperator() {
        return operator;
    }

}
