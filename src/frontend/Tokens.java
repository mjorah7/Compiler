package frontend;

import java.util.ArrayList;
import java.util.List;

public class Tokens {

    private List<Token> tokenList = new ArrayList<>();
    private int position = -1;

    public Tokens (List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public boolean reachedEnd () {
        return this.position >= this.tokenList.size();
    }

    public Token getCurToken () {
        if (this.reachedEnd() || this.position < 0) {
            return null;
        }
        return this.tokenList.get(this.position);
    }

    public Token getNextToken () {
        this.position ++;
        if (this.reachedEnd()) {
            return null;
        }
        return this.tokenList.get(this.position);
    }

    public Token forward () {
        return this.forward(1);
    }

    public Token forward (int offset) {
        if (this.position + offset >= this.tokenList.size()) {
            return null;
        }
        return this.tokenList.get(this.position + offset);
    }

    public int remainSize () {
        return this.tokenList.size() - this.position - 1;
    }

    public int getPosition () {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
