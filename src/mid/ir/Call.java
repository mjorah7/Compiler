package mid.ir;

import frontend.Symbol;

import java.util.List;
import java.util.StringJoiner;

public class Call extends Instruction {

    private final Symbol func;
    private final List<Operand> args;
    private final Symbol ret;

    public Call(Symbol func, List<Operand> args, Symbol ret) {
        this.func = func;
        this.args = args;
        this.ret = ret;
    }

    @Override
    public String toIrString() {
        StringBuilder sb = new StringBuilder();
        if (ret == null) {
            sb.append(table()).append("call ");
        } else {
            sb.append(table()).append(ret.value2Ir()).append(" = call ");
        }
        sb.append(Symbol.type2Ir.get(func.symbolValueType)).append(" ").append("@").append(func.symbolName).append("(");

        StringJoiner sj = new StringJoiner(",");
        for (Operand arg : args) {
            sj.add(arg.all2Ir());
        }
        sb.append(sj);

        sb.append(")");
        return sb.toString();
    }

}
