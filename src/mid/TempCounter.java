package mid;

import frontend.Symbol;
import mid.ir.Number;
import mid.ir.Operand;

import java.util.ArrayList;
import java.util.List;

public class TempCounter {

    private int cnt = 0;

    public Symbol getTemp(int level) {
        return new Symbol("-t" + (cnt++), Symbol.VarType.VAR, Symbol.ValueType.INT, false, null, level);
    }

    public Symbol getPointer(int level, Symbol base, List<Operand> index) {
        List<Integer> dim = new ArrayList<>();
        dim.add(0);
        dim.addAll(base.dimensions);
        if (index.size() > 1) {
            if (index.get(index.size()-1) instanceof Number && ((Number) index.get(index.size()-1)).getValue() == -1) {
                dim.subList(1, index.size() - 1).clear();
            } else {
                dim.subList(1, index.size()).clear();
            }
        }
        return new Symbol(Symbol.VarType.POINTER, Symbol.ValueType.INT, "-t" + (cnt ++), dim, level);
    }

}
