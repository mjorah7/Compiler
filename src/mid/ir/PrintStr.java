package mid.ir;

import java.util.StringJoiner;

public class PrintStr extends Instruction {

    private final String string;

    public PrintStr(String string) {
        this.string = string;
    }

    @Override
    public String toIrString() {
        StringJoiner sj = new StringJoiner("\n");
        for (int i = 0; i < string.length(); i++) {
            if (i + 1 < string.length() && string.charAt(i) == '\\' && string.charAt(i+1) == 'n') {
                sj.add(table() + "call void @putch(i32 " + (int) '\n' + ")");
                i ++;
            } else {
                sj.add(table() + "call void @putch(i32 " + (int) string.charAt(i) + ")");
            }
        }
        return sj.toString();
    }

}
