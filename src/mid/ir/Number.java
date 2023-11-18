package mid.ir;

public class Number implements Operand {

    private final int number;

    public Number(int number) {
        this.number = number;
    }

    @Override
    public String value2Ir() {
        return String.valueOf(this.number);
    }

    @Override
    public String type2Ir() {
        return "i32";
    }

    @Override
    public String all2Ir() {
        return this.type2Ir() + " " + this.value2Ir();
    }

    public int getValue() {
        return this.number;
    }

}
