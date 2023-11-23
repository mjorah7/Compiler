package mid.ir;

import frontend.Symbol;

public class PointerOp extends Instruction {

    public enum Op {
        LOAD, STORE
    }

    private final Op operator;
    private final Operand src;
    private final Symbol dst;

    public PointerOp(Op operator, Symbol dst, Operand src) {
        super();
        this.operator = operator;
        this.src = src;
        this.dst = dst;
    }

    @Override
    public String toIrString() {
        if (src instanceof Symbol && ((Symbol) src).isParam){
            if (this.operator == Op.LOAD) {
                return table() + dst.value2Ir() + " = load " +
                        dst.type2Ir() + ", " +
                        ((Symbol) src).paramSymbol.type2Ir() + "* " + ((Symbol) src).paramSymbol.value2Ir();
            } else {
                return table() + "store " + src.type2Ir() + " " + src.value2Ir() + ", "
                        + dst.type2Ir() + "* " + dst.value2Ir();
            }
        } else if (dst.isParam) {
            if (this.operator == Op.LOAD) {
                return table() + dst.value2Ir() + " = load " +
                        dst.type2Ir() + ", " +
                        src.type2Ir() + "* " + src.value2Ir();
            } else {
                return table() + "store " + src.type2Ir() + " " + src.value2Ir() + ", "
                        + dst.paramSymbol.type2Ir() + "* " + dst.paramSymbol.value2Ir();
            }
        } else {
            if (this.operator == Op.LOAD) {
                return table() + dst.value2Ir() + " = load " +
                        dst.type2Ir() + ", " +
                        src.type2Ir() + (src.type2Ir().contains("*") ? " " : "* ") + src.value2Ir();
            } else {
                return table() + "store " + src.type2Ir() + " " + src.value2Ir() + ", "
                        + dst.type2Ir() + (dst.isPointer() ? " " : "* ") + dst.value2Ir();
            }
        }
    }

    public Op getOperator() {
        return operator;
    }

    public Operand getSrc() {
        return src;
    }

    public Symbol getDst() {
        return dst;
    }

}
