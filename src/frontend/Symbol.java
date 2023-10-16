package frontend;

import java.util.List;

public class Symbol {

    public enum VarType {
        VAR, ARRAY1, ARRAY2, FUNC, ERROR, DEBUG
    }

    public enum ValueType {
        INT, VOID,
    }

    public String symbolName;

    public VarType symbolVarType;

    public ValueType symbolValueType;

    public boolean isConst;

    // for func
    public List<VarType> paramsType;

    public Symbol(String symbolName, VarType symbolVarType, ValueType symbolValueType, boolean isConst, List<VarType> paramsType) {
        this.symbolName = symbolName;
        this.symbolVarType = symbolVarType;
        this.symbolValueType = symbolValueType;
        this.isConst = isConst;
        this.paramsType = paramsType;
    }
}
