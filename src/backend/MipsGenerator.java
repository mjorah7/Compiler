package backend;

import config.Config;
import frontend.Symbol;
import mid.IrModule;
import mid.ir.*;
import mid.ir.Number;
import utils.Pair;
import utils.Triple;

import java.awt.print.PrinterGraphics;
import java.nio.FloatBuffer;
import java.util.*;

public class MipsGenerator {

    public static boolean withAt = true;

    public enum Reg {
        $zero,
        $at,
        $v0, $v1,
        $a0, $a1, $a2, $a3,
        $t0, $t1, $t2, $t3, $t4, $t5, $t6, $t7, $t8, $t9,
        $s0, $s1, $s2, $s3, $s4, $s5, $s6, $s7,
        $k0, $k1,
        $gp,
        $sp,
        $fp,
        $ra;

        @Override
        public String toString() {
            return name();
        }
    }

    class Memory {
        private final Map<String, Pair<Reg, Integer>> mem = new HashMap<>();  // ir symbol name, reg name, offset
        private int gpOffset = 0;
        private int spOffset = 0;

        public Integer getOffset(String irName) {
            assert mem.containsKey(irName) : "error in getOffset";
            return mem.get(irName).getSecond();
        }

        public void getGp(String name) {
            if (!mem.containsKey(name)) {
                mem.put(name, new Pair<>(Reg.$gp, gpOffset));
                gpOffset += 4;
            }
        }

        public void getGpArray(String name, int offset) {
            if (!mem.containsKey(name)) {
                getGp(name);
                writeMips("addu $t0, $gp, " + gpOffset);
                store(Reg.$t0, name);
                gpOffset += offset;
            }
        }

        public void getSp(String name) {
            if (!mem.containsKey(name)) {
                spOffset -= 4;
                mem.put(name, new Pair<>(Reg.$sp, spOffset));
            }
        }

        public void getSpArray(String name, int offset) {
            if (!mem.containsKey(name)) {
                getSp(name);
                spOffset -= offset;
                writeMips("addu $t0, $sp, " + spOffset);
                store(Reg.$t0, name);
            }
        }

        public void load(Reg dst, Reg src, int offset) {
            writeMips("lw " + dst + ", " + offset + "(" + src + ")");
        }

        public void load(Reg dst, String irName) {
            if (irName.matches("-?[0-9]+")) {
                writeMips("li " + dst + ", " + irName);
            } else {
                writeMips("lw " + dst + ", " + mem.get(irName).getSecond() + "(" + mem.get(irName).getFirst() + ")");
            }
        }

        public void store(Reg src, Reg dst, int offset) {
            writeMips("sw " + src + ", " + offset + "(" + dst + ")");
        }

        public void store(Reg src, String irName) {
            writeMips("sw " + src + ", " + mem.get(irName).getSecond() + "(" + mem.get(irName).getFirst() + ")");
        }

        public void printMemory() {
            System.out.println("===============================================");
            for (String s : mem.keySet()) {
                System.out.println(s + " " + mem.get(s).getFirst() + " " + mem.get(s).getSecond());
            }
            System.out.println("===============================================");
        }
    }

    private final IrModule irModule;
    private final Memory memory = new Memory();
    private final StringJoiner mipsString = new StringJoiner("\n");
    private Instruction lastInstruction;

    public MipsGenerator(IrModule irModule) {
        this.irModule = irModule;
        withAt = false;
    }

    public String generateMips() {
        // data segment
//        writeMips(".data");
//        for (Symbol symbol : irModule.getVarDefs()) {
//            if (symbol.isVar()) {
//                memory.getGp(symbol.value2Ir());
//                if (symbol.initValue != null) {
//                    writeMips(symbol.value2Ir() + ": .word " + ((Number) symbol.initValue).getValue());
//                } else {
//                    writeMips(symbol.value2Ir() + ": .word 0");
//                }
//            }
//            if (symbol.isArray()) {
//                memory.getGpArray(symbol.value2Ir(), (symbol.dimensions.size() == 2 ? symbol.dimensions.get(0) * symbol.dimensions.get(1) : symbol.dimensions.get(0)) * 4);
//                StringJoiner sj = new StringJoiner(", ");
//                for (int i = (symbol.dimensions.size() == 2 ? symbol.dimensions.get(0) * symbol.dimensions.get(1) : symbol.dimensions.get(0)) - 1 ; i >= 0 ; i --) {
//                    if (symbol.initValues.size() == 0) {
//                        sj.add("0");
//                    } else {
//                        sj.add(symbol.initValues.get(i).value2Ir());
//                    }
//                }
//                writeMips(symbol.value2Ir() + ": .word "  + sj);
//            }
//        }

        // libraries
        writeMips("");
        writeMips(".macro GETINT\n\tli $v0, 5\n\tsyscall\n.end_macro");
        writeMips(".macro PUTINT\n\tli $v0, 1\n\tsyscall\n.end_macro");
        writeMips(".macro PUTCH\n\tli $v0, 11\n\tsyscall\n.end_macro");
        writeMips(".macro PUTSTR\n\tli $v0, 4\n\tsyscall\n.end_macro");

        // text segment
        writeMips("");
        writeMips(".text");

        for (Symbol symbol : irModule.getVarDefs()) {
            if (symbol.isVar()) {
                memory.getGp(symbol.value2Ir());
                if (symbol.initValue != null) {
                    memory.load(Reg.$t2, symbol.initValue.value2Ir());
                    memory.store(Reg.$t2, symbol.value2Ir());
                } else {
                    memory.load(Reg.$t2, "0");
                    memory.store(Reg.$t2, symbol.value2Ir());
                }
            }
            if (symbol.isArray()) {
                memory.getGpArray(symbol.value2Ir(), symbol.getCapacity());
                for (int i = 0 ; i < symbol.getCapacity() / 4 ; i ++) {
                    if (symbol.initValues.size() != 0) {
                        memory.load(Reg.$t2, symbol.initValues.get(i).value2Ir());
                        memory.load(Reg.$t0, symbol.value2Ir());
                        writeMips("addu $t0, $t0, " + (4 * i));
                        memory.store(Reg.$t2, Reg.$t0, 0);
                    } else {
//                        memory.load(Reg.$t2, "0");
                    }
                }
            }
        }

        writeMips("jal main");
        writeMips("li " + Reg.$v0 + ", 10");
        writeMips("syscall");

        for (FuncDef funcDef : irModule.getFuncDefs()) {
            writeMips("");

            // func name
            writeMips(funcDef.symbol.value2Ir() + ":");

            // func params
            if (funcDef.symbol.params != null) {
                for (int i = 0; i < funcDef.symbol.params.size(); i++) {
                    memory.load(Reg.$t0, Reg.$sp, 4 * (funcDef.symbol.params.size() - 1 - i));
                    memory.getSp(funcDef.symbol.params.get(i).value2Ir());
                    memory.store(Reg.$t0, funcDef.symbol.params.get(i).value2Ir());
                }
            }

            // func basic blocks
            for (BasicBlock basicBlock : funcDef.basicBlocks) {
                writeMips(basicBlock.beginLabel + ":");

                // basic block instructions
                lastInstruction = null;
                Instruction instruction = basicBlock.getFirstInstruction();
                while(instruction != basicBlock.getEndInstruction()) {
                    if (!(instruction instanceof PrintStr)) {
                        writeMips("# " + instruction.toIrString());
                    }
                    translate(instruction);
                    writeMips("");

                    lastInstruction = instruction;
                    instruction = instruction.getNextInstruction();
                }
            }
        }
        if (Config.DEBUG) {
            memory.printMemory();
        }
        return mipsString.toString();
    }

    private void translate(Instruction instruction) {
        if (instruction instanceof BinaryOperator) {
            instructionBinaryOperator((BinaryOperator)instruction);
        } else if (instruction instanceof Branch) {
            translateBranch((Branch)instruction);
        } else if (instruction instanceof Call) {
            translateCall((Call)instruction);
        } else if (instruction instanceof ElementPtr) {
            translateElementPtr((ElementPtr)instruction);
        } else if (instruction instanceof Input) {
            translateInput((Input)instruction);
        } else if (instruction instanceof Jump) {
            translateJump((Jump)instruction);
        } else if (instruction instanceof Pass) {
            translatePass((Pass)instruction);
        } else if (instruction instanceof PointerOp) {
            translatePointerOp((PointerOp)instruction);
        } else if (instruction instanceof PrintInt) {
            translatePrintInt((PrintInt)instruction);
        } else if (instruction instanceof PrintStr) {
            translatePrintStr((PrintStr)instruction);
        } else if (instruction instanceof Return) {
            translateReturn((Return)instruction);
        } else if (instruction instanceof Trunc) {
            translateTrunc((Trunc)instruction);
        } else if (instruction instanceof UnaryOperator) {
            translateUnaryOperator((UnaryOperator)instruction);
        } else if (instruction instanceof VarDef) {
            translateVarDef((VarDef)instruction);
        } else if (instruction instanceof Zext) {
            translateZext((Zext)instruction);
        } else {
            assert false : "error in translate";
        }
    }

    private void instructionBinaryOperator(BinaryOperator instruction) {
        memory.getSp(instruction.getRes().value2Ir());  // register result reg
        memory.load(Reg.$t0, instruction.getLeft().value2Ir());
        memory.load(Reg.$t1, instruction.getRight().value2Ir());
        if (!(instruction.getOperator() == BinaryOperator.Op.MOD)){
            writeMips(BinaryOperator.op2mips.get(instruction.getOperator()) + " $t2, $t0, $t1");
        } else {
            writeMips("div $t0, $t1");
            writeMips("mfhi $t2");
        }
        memory.store(Reg.$t2, instruction.getRes().value2Ir());
    }

    private void translateBranch(Branch instruction) {
        memory.load(Reg.$t0, instruction.getCondition().value2Ir());
        writeMips("beqz $t0, " + instruction.getLabelFalse());
        writeMips("j " + instruction.getLabelTrue());
    }

    private void translateCall(Call instruction) {
        if (instruction.getRet() != null) {
            memory.getSp(instruction.getRet().value2Ir());
        }  // register result reg
        Symbol func = instruction.getFunc();
        memory.store(Reg.$ra, Reg.$sp, memory.spOffset - 4);
        for (int i = 0 ; i < instruction.getArgs().size() ; i ++) {
            memory.load(Reg.$t0, instruction.getArgs().get(i).value2Ir());
            memory.store(Reg.$t0, Reg.$sp, memory.spOffset - 4 * (i + 2));
        }
        writeMips("addu $sp, $sp, " + (memory.spOffset - 4 * (1 + instruction.getArgs().size())));
        writeMips("jal " + func.value2Ir());
        writeMips("addu $sp, $sp, " + (-memory.spOffset + 4 * (1 + instruction.getArgs().size())));
        memory.load(Reg.$ra, Reg.$sp, memory.spOffset - 4);
        if (instruction.getRet() != null) {
            memory.store(Reg.$v0, instruction.getRet().value2Ir());
        }
    }

    private void translateElementPtr(ElementPtr instruction) {
        Symbol base = instruction.getBase();
        Symbol dst = instruction.getDst();
        List<Operand> index = instruction.getIndex();

        memory.getSp(dst.value2Ir());

        if (base.isArray1()) {
            writeMips("li $t0, 0");
            if (index.size() >= 2) {
                memory.load(Reg.$t0, index.get(1).value2Ir());
            }
            writeMips("li $t1, 4");
            writeMips("mul $t0, $t0, $t1");  // offset in $t0
            memory.load(Reg.$t1, base.value2Ir());
            writeMips("addu $t0, $t0, $t1");
            memory.store(Reg.$t0, dst.value2Ir());
        } else if (base.isArray2()) {
            writeMips("li $t0, 0");
            if (index.size() >= 2) {
                memory.load(Reg.$t0, index.get(1).value2Ir());
            }
            writeMips("mul $t0, $t0, " + base.dimensions.get(1).toString());
            if (index.size() >= 3){
                memory.load(Reg.$t1, index.get(2).value2Ir());
                writeMips("addu $t0, $t0, $t1");
            }
            writeMips("li $t1, 4");
            writeMips("mul $t0, $t0, $t1");  // offset in $t0
            memory.load(Reg.$t1, base.value2Ir());
            writeMips("addu $t0, $t0, $t1");
            memory.store(Reg.$t0, dst.value2Ir());
        } else if (base.isPointer()) {
            if (base.dimensions.size() == 1) {
                writeMips("li $t0, 0");
                if (index.size() >= 1) {
                    memory.load(Reg.$t0, index.get(0).value2Ir());
                }
                writeMips("li $t1, 4");
                writeMips("mul $t0, $t0, $t1");  // offset in $t0

                memory.load(Reg.$t2, base.value2Ir());
                writeMips("addu $t0, $t0, $t2");  // total offset in $t0
                memory.store(Reg.$t0, dst.value2Ir());
            } else {
                writeMips("li $t0, 0");
                if (index.size() >= 1) {
                    memory.load(Reg.$t0, index.get(0).value2Ir());
                }
                writeMips("mul $t0, $t0, " + base.dimensions.get(1).toString());
                if (index.size() >= 2) {
                    memory.load(Reg.$t1, index.get(1).value2Ir());
                    writeMips("addu $t0, $t0, $t1");
                }
                writeMips("li $t1, 4");
                writeMips("mul $t0, $t0, $t1");  // offset in $t0
                memory.load(Reg.$t2, base.value2Ir());
                writeMips("addu $t0, $t0, $t2");  // total offset in $t0
                memory.store(Reg.$t0, dst.value2Ir());
            }
        }
    }

    private void translateInput(Input instruction) {
        memory.getSp(instruction.getDst().value2Ir());
        writeMips("GETINT");
        memory.store(Reg.$v0, instruction.getDst().value2Ir());
    }

    private void translateJump(Jump instruction) {
        writeMips("j " + instruction.getLabel());
    }

    private void translatePass(Instruction instruction) {

    }

    private void translatePointerOp(PointerOp instruction) {
        if ((lastInstruction instanceof ElementPtr)) {
            if (instruction.getOperator() == PointerOp.Op.LOAD && Objects.equals(((ElementPtr) lastInstruction).getDst().value2Ir(), instruction.getSrc().value2Ir())) {
                memory.getSp(instruction.getDst().value2Ir());
                memory.load(Reg.$t0, instruction.getSrc().value2Ir());
                memory.load(Reg.$t1, Reg.$t0, 0);
                memory.store(Reg.$t1, instruction.getDst().value2Ir());
            } else if (instruction.getOperator() == PointerOp.Op.STORE && Objects.equals(((ElementPtr) lastInstruction).getDst().value2Ir(), instruction.getDst().value2Ir())) {
                memory.load(Reg.$t0, instruction.getSrc().value2Ir());
                memory.load(Reg.$t1, instruction.getDst().value2Ir());
                memory.store(Reg.$t0, Reg.$t1, 0);
            } else {
                if (instruction.getOperator() == PointerOp.Op.LOAD) {
                    memory.getSp(instruction.getDst().value2Ir());  // register result reg
                }
                memory.load(Reg.$t0, instruction.getSrc().value2Ir());
                memory.store(Reg.$t0, instruction.getDst().value2Ir());
            }
        } else {
            if (instruction.getOperator() == PointerOp.Op.LOAD) {
                memory.getSp(instruction.getDst().value2Ir());  // register result reg
            }
            memory.load(Reg.$t0, instruction.getSrc().value2Ir());
            memory.store(Reg.$t0, instruction.getDst().value2Ir());
        }
    }

    private void translatePrintInt(PrintInt instruction) {
        memory.load(Reg.$a0, instruction.getOperand().value2Ir());
        writeMips("PUTINT");
    }

    private void translatePrintStr(PrintStr instruction) {
        for (int i = 0; i < instruction.getString().length(); i++) {
            if (i + 1 < instruction.getString().length() && instruction.getString().charAt(i) == '\\' && instruction.getString().charAt(i+1) == 'n') {
                writeMips("li $a0, 10");
                writeMips("PUTCH");
                i ++;
            } else {
                writeMips("li $a0, " + (int)instruction.getString().charAt(i));
                writeMips("PUTCH");
            }
        }
    }

    private void translateReturn(Return instruction) {
        if (instruction.getReturnValue() != null) {
            memory.load(Reg.$v0, instruction.getReturnValue().value2Ir());
        }
        writeMips("jr $ra");
    }

    private void translateTrunc(Trunc instruction) {
        memory.getSp(instruction.getDst().value2Ir());
        memory.load(Reg.$t0, instruction.getSrc().value2Ir());
        memory.store(Reg.$t0, instruction.getDst().value2Ir());
    }

    private int notLabelCounter = 0;
    private void translateUnaryOperator(UnaryOperator instruction) {
        memory.getSp(instruction.getRes().value2Ir());
        if (instruction.getOperator() == UnaryOperator.Op.PLUS) {
            memory.load(Reg.$t0, instruction.getRight().value2Ir());
            memory.store(Reg.$t0, instruction.getRes().value2Ir());
        }
        if (instruction.getOperator() == UnaryOperator.Op.MINU) {
            memory.load(Reg.$t0, instruction.getRight().value2Ir());
            writeMips("li $t1, 0");
            writeMips("subu $t2, $t1, $t0");
            memory.store(Reg.$t2, instruction.getRes().value2Ir());
        }
        if (instruction.getOperator() == UnaryOperator.Op.NOT) {
            int label1 = (notLabelCounter ++);
            int label2 = (notLabelCounter ++);

            memory.load(Reg.$t0, instruction.getRight().value2Ir());
            writeMips("beqz $t0, not_label_" + label1);
            writeMips("li $t1, 0");
            writeMips("j not_label_" + label2);
            writeMips("not_label_" + label1 + ":");
            writeMips("li $t1, 1");
            writeMips("not_label_" + label2 + ":");
            memory.store(Reg.$t1, instruction.getRes().value2Ir());
        }
    }

    private void translateVarDef(VarDef instruction) {
        if (instruction.getSymbol().isVar()) {
            memory.getSp(instruction.getSymbol().value2Ir());  // register result reg
        } else {
            if (instruction.getSymbol().isArray1()) {
                memory.getSpArray(instruction.getSymbol().value2Ir(), 4 * instruction.getSymbol().dimensions.get(0));
            }
            if (instruction.getSymbol().isArray2()) {
                memory.getSpArray(instruction.getSymbol().value2Ir(), 4 * instruction.getSymbol().dimensions.get(0) * instruction.getSymbol().dimensions.get(1));
            }
            if (instruction.getSymbol().isPointer()) {
                assert false : "pointer type in translateVarDef";
            }
        }
    }

    private void translateZext(Zext instruction) {
        memory.getSp(instruction.getDst().value2Ir());
        memory.load(Reg.$t0, instruction.getSrc().value2Ir());
        writeMips("sll $t0, $t0, 16");
        writeMips("srl $t0, $t0, 16");
        memory.store(Reg.$t0, instruction.getDst().value2Ir());
    }

    //------------------------------------------------------------------------------------------------------------------
    // below are helper functions

    private void writeMips(String string) {
        if (string.indexOf('.') != -1 || string.indexOf(':') != -1) {
            mipsString.add(string);
        } else {
            mipsString.add("\t" + string);
        }
    }

}
