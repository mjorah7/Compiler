package frontend;

public class Error implements Comparable<Error> {

    public enum ErrorType {
        a, b, c, d, e, f, g, h, i, j, k, l, m,
    }

    public int line;

    public ErrorType errorType;

    public Error (int line, ErrorType errorType) {
        this.line = line;
        this.errorType = errorType;
    }

    @Override
    public int compareTo(Error otherError) {
        return Integer.compare(this.line, otherError.line);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Error newError = (Error) obj;
        return line == newError.line && errorType.equals(newError.errorType);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(line);
    }

}
