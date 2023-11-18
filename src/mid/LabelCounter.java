package mid;

public class Label {

    private int cnt = 0;

    public String getLabel() {
        String ret =  "label" + cnt;
        cnt ++;
        return ret;
    }

}
