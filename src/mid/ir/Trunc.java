package mid.ir;

public class Trunc extends Instruction {

    private final Operand src;
    private final Operand dst;
    private final String type;

    public Trunc(Operand src, Operand dst, String type) {
        this.src = src;
        this.dst = dst;
        this.type = type;
    }

    @Override
    public String toIrString() {
        return table() + dst.value2Ir() + " = icmp ne " + type + " " + src.value2Ir() + ", 0";
    }

    public Operand getSrc() {
        return src;
    }

    public Operand getDst() {
        return dst;
    }

    public String getType() {
        return type;
    }

}
