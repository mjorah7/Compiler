package mid.ir;

import config.Config;
import frontend.Symbol;

import java.util.StringJoiner;

public class MainFuncDef extends FuncDef {

    public MainFuncDef(Symbol symbol) {
        super(symbol);
    }

    @Override
    public String toIrString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("define dso_local i32 @main() {");
        for (BasicBlock basicBlock : basicBlocks) {
            String br = table() + "br label %" + basicBlock.beginLabel;
            if (Config.LLVM) {
                sj.add(br);
            }
            sj.add(basicBlock.toIrString());
        }
        sj.add("}");
        return sj.toString();
    }

}
