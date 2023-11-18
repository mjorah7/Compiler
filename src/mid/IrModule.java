package mid;

import frontend.Symbol;
import mid.ir.FuncDef;
import mid.ir.VarDef;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class IrModule {

    private final List<String> target = new ArrayList<>() {
        {
            add("declare i32 @getint()");
            add("declare void @putint(i32)");
            add("declare void @putch(i32)");
            add("declare void @putstr(i8*)");
        }
    };
    private final List<Symbol> varDefs = new ArrayList<>();
    private final List<FuncDef> funcDefs = new ArrayList<>();

    public String toIrString () {
        StringJoiner sj = new StringJoiner("\n");
        for (String str : target) {
            sj.add(str);
        }
        if (varDefs.size() != 0) {
            sj.add("");
        }
        for (Symbol varDef : varDefs) {
            sj.add(varDef.toIrString());
        }
        if (funcDefs.size() != 0) {
            sj.add("");
        }
        for (FuncDef funcDef : funcDefs) {
            if (funcDef != funcDefs.get(0)) {
                sj.add("");
            }
            sj.add(funcDef.toIrString());
        }
        return sj.toString();
    }

    public void addVarDef(Symbol varDef) {
        this.varDefs.add(varDef);
    }

    public void addFuncDef(FuncDef funcDef) {
        this.funcDefs.add(funcDef);
    }

}
