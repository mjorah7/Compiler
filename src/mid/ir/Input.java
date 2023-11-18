package mid.ir;

import frontend.Symbol;

public class Input extends Instruction {

    private final Symbol dst;

    public Input(Symbol dst) {
        super();
        this.dst = dst;
    }

    @Override
    public String toIrString() {
        return table() + dst.value2Ir() + " = call i32 @getint()";
    }

}
