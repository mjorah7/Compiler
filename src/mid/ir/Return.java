package mid.ir;

public class Return extends Instruction {

    private final Operand returnValue;

    public Return(Operand returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toIrString() {
        if (returnValue == null) {
            return table() + "ret void";
        } else {
            return table() + "ret" + " " + returnValue.all2Ir();
        }
    }

}
