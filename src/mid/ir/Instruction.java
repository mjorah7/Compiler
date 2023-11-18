package mid.ir;

public abstract class Instruction {

    private Instruction prevInstruction;
    private Instruction nextInstruction;
    private final int instructionId;
    private static int instructionIdCounter = 0;

    public Instruction () {
        this.instructionId = instructionIdCounter;
        instructionIdCounter ++;
    }

    public Instruction getPrevInstruction() {
        return prevInstruction;
    }

    public Instruction getNextInstruction() {
        return nextInstruction;
    }

    public int getInstructionId() {
        return instructionId;
    }

    public void setPrevInstruction(Instruction prevInstruction) {
        this.prevInstruction = prevInstruction;
    }

    public void setNextInstruction(Instruction nextInstruction) {
        this.nextInstruction = nextInstruction;
    }

    public boolean hasPrevInstruction () {
        return this.prevInstruction != null;
    }

    public boolean hasNextInstruction () {
        return !(this.nextInstruction instanceof Pass);
    }

    public void addToChain (Instruction instruction) {
        Instruction self = this;
        while (self.hasNextInstruction()) {
            self = self.getNextInstruction();
        }

        Instruction pass = self.getNextInstruction();

        self.setNextInstruction(instruction);
        instruction.setPrevInstruction(self);

        self.nextInstruction.setNextInstruction(pass);
        pass.setPrevInstruction(self.nextInstruction);
    }

    // for subclass
    public String toIrString () {
        return "Instruction";
    }

    public String table() {
        return "\t";
    }

}
