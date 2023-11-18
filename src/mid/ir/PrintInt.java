package mid.ir;

import frontend.Symbol;

public class PrintInt extends Instruction {

    private final Operand operand;

    public PrintInt(Operand operand) {
        super();
        this.operand = operand;
    }

    @Override
    public String toIrString() {
        return table() + "call void @putint(" + ((operand instanceof Number) ? "i32" : Symbol.type2Ir.get(((Symbol) operand).symbolValueType)) + " " + operand.value2Ir() + ")";
    }

}
