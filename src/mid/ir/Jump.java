package mid.ir;

public class Jump extends Instruction {

    private final String label;

    public Jump(String label) {
        super();
        this.label = label;
    }

    @Override
    public String toIrString() {
        return table() + "br label %" + label;
    }

}
