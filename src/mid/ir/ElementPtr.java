package mid.ir;

import frontend.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ElementPtr extends Instruction {

    private final Symbol dst;
    private final Symbol base;
    private final List<Operand> index = new ArrayList<>();

    public ElementPtr(Symbol dst, Symbol base, List<Operand> index) {
        this.dst = dst;
        this.base = base;
//        if (base.isPointer()) {
//            this.index.add(new Number(0));
//        }
        this.index.addAll(index);
    }

    @Override
    public String toIrString() {
        StringJoiner sj = new StringJoiner(", ");
        for (Operand operand : index) {
            if (operand instanceof Number) {
                sj.add("i32 " + ((Number) operand).getValue());
            } else {
                sj.add("i32 " + operand.value2Ir());
            }
        }

        if (base.isParam) {
            return table() + dst.value2Ir() + " = getelementptr " + base.type2ParamEleIr() + ", " + base.type2ParamEleIr() + "* " + base.value2Ir() + ", " + sj;
        } else {
            return table() + dst.value2Ir() + " = getelementptr " + base.type2EleIr() + ", " + base.type2EleIr() + "* " + base.value2Ir() + ", " + sj;
        }
    }

    public Symbol getDst() {
        return dst;
    }

    public Symbol getBase() {
        return base;
    }

    public List<Operand> getIndex() {
        return index;
    }

}
