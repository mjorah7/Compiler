package mid.ir;

import mid.IdCounter;

import java.util.StringJoiner;

public class BasicBlock extends Instruction {

    public String beginLabel;
    private String endLabel;
    private Instruction endInstruction;
    private IdCounter idCounter;

    public BasicBlock(String beginLabel) {
        this.beginLabel = beginLabel;
        this.endLabel = null;
        this.endInstruction = new Pass();
        this.setNextInstruction(this.endInstruction);
        this.endInstruction.setPrevInstruction(this);
    }

    @Override
    public String toIrString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(beginLabel + ":");
        Instruction instruction = this.getNextInstruction();
        while(instruction != endInstruction) {
            sj.add(instruction.toIrString());
            instruction = instruction.getNextInstruction();
        }
        if (endLabel != null) {
            sj.add(endLabel + ":");
        }
        return sj.toString();
    }

    public Instruction getLastInstruction() {
        return this.endInstruction.getPrevInstruction();
    }

    public Instruction getFirstInstruction() {
        return this.getNextInstruction();
    }

    public Instruction getEndInstruction() {
        return this.endInstruction;
    }

    public void addToFront(Instruction instruction) {
        Instruction pre = this.getNextInstruction();
        this.setNextInstruction(instruction);
        instruction.setNextInstruction(pre);
        pre.setPrevInstruction(instruction);
        instruction.setPrevInstruction(this);
    }

}
