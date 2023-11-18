package mid.ir;

import config.Config;
import frontend.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class FuncDef extends Instruction {

    public Symbol symbol;
    public List<BasicBlock> basicBlocks = new ArrayList<>();

    public FuncDef(Symbol symbol) {
        this.symbol = symbol;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        this.basicBlocks.add(basicBlock);
    }

    @Override
    public String toIrString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(symbol.toFuncIrString() + " {");
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

    public boolean isParam(String name) {
        if (this.symbol.params == null) {
            return false;
        }
        for (Symbol s : this.symbol.params) {
            if (Objects.equals(s.symbolName, name)) {
                return true;
            }
        }
        return false;
    }

}
