package mid.ir;

import frontend.Symbol;

public class VarDef extends Instruction {

    private final Symbol symbol;

    public VarDef(Symbol symbol) {
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return this.symbol;
    }

    @Override
    public String toIrString() {
        return table() + symbol.value2Ir() + " = alloca " + symbol.type2Ir();
    }

}
