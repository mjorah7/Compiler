package mid.ir;

public class Zext extends Instruction {

    private final Operand src;
    private final Operand dst;

    public Zext(Operand src, Operand dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public String toIrString() {
        return table() + dst.value2Ir() + " = zext i1 " + src.value2Ir() + " to i32";
    }

    public Operand getSrc() {
        return src;
    }

    public Operand getDst() {
        return dst;
    }

}
