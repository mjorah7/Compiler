package mid;

public class LabelCounter {

    private int cnt = 0;

    public String getLabel() {
        String ret =  "label_" + cnt;
        cnt ++;
        return ret;
    }

    public String getLabel(String note) {
        String ret =  "label_" + note + "_" + cnt;
        cnt ++;
        return ret;
    }

}
