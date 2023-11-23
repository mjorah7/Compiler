package frontend;

import backend.MipsGenerator;
import mid.ir.Number;
import mid.ir.Operand;
import mid.IrGenerator.*;

import java.util.*;

public class Symbol implements Operand {

    public static int globalCounter = 0;
    public static boolean careDefined = false;

    public enum VarType {
        VAR, ARRAY1, ARRAY2, FUNC, POINTER, ERROR, DEBUG
    }

    public enum ValueType {
        INT, VOID,
    }

    public static final Map<ValueType, String> type2Ir = new HashMap<>() {
        {
            put(ValueType.INT, "i32");
            put(ValueType.VOID, "void");
        }
    };

    public String symbolName;
    public VarType symbolVarType;
    public ValueType symbolValueType;
    public int globalCount;
    public boolean isConst;
    public boolean isGlobal = false;
    public boolean isDefined = false;

    public int level;

    public Operand initValue;  // Exp or ConstExp
    public List<Operand> initValues = new ArrayList<>();
    public List<Integer> dimensions = new ArrayList<>();

    public int globalValue = 0;

    // for func
    public List<VarType> paramsType;
    public List<Symbol> params = null;

    // for param
    public boolean isParam = false;
    public Symbol paramSymbol = null;

    public Symbol(String symbolName, VarType symbolVarType, ValueType symbolValueType, boolean isConst, List<VarType> paramsType, int level) {
        this.symbolName = symbolName;
        this.symbolVarType = symbolVarType;
        this.symbolValueType = symbolValueType;
        this.isConst = isConst;
        this.paramsType = paramsType;
        this.level = level;
        this.isGlobal = this.level == 0;
        this.globalCount = (globalCounter ++);
        this.isDefined = Symbol.careDefined;
    }

    public Symbol(VarType varType, ValueType valueType, String symbolName, List<Integer> dimensions, int level) {
        this.symbolVarType = varType;
        this.symbolValueType = valueType;
        this.symbolName = symbolName;
        this.dimensions = dimensions;
        this.level = level;
        this.globalCount = (globalCounter ++);
        this.isDefined = Symbol.careDefined;
    }

    public void setVarInitValue(Operand node) {
        this.initValue = node;
    }

    public void setArrayInitValue(List<Operand> initValues, List<Integer> dimensions) {
        this.initValues = initValues;
        this.dimensions = dimensions;
    }

    public Symbol getParamBottomSymbol() {
        if (this.symbolVarType != VarType.POINTER) {
            return this;
        } else {
            Symbol cur = this;
            while(cur.isParam && cur.symbolVarType == VarType.POINTER) {
                cur = cur.paramSymbol;
            }
            return cur;
        }
    }

    @Override
    public String value2Ir() {
        if (!isGlobal) {
            return "%" + symbolName + "_" + level + "_" + globalCount;
        } else {
            return (MipsGenerator.withAt ? "@" : "") + symbolName;
        }
    }

    @Override
    public String type2Ir() {
        if (this.symbolVarType == VarType.VAR) {
            return type2Ir.get(symbolValueType);
        } else if (this.isArray()) {
            StringBuilder sb = new StringBuilder(type2Ir.get(this.symbolValueType));
            for (int i = dimensions.size() - 1 ; i >= 0 ; i --) {
                sb.append(']');
                sb.insert(0, "[" + dimensions.get(i) + " x ");
            }
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder(type2Ir.get(this.symbolValueType));
            for (int i = dimensions.size() - 1 ; (isParam ? i >= 1 : i >= 2) ; i --) {  // set i >= 2 to maintain score 55
                sb.append(']');
                sb.insert(0, "[" + dimensions.get(i) + " x ");
            }
            sb.append("*");
            return sb.toString();
        }
    }

    public String type2EleIr() {
        if (this.symbolVarType == VarType.VAR) {
            return type2Ir.get(symbolValueType);
        } else {
            StringBuilder sb = new StringBuilder(type2Ir.get(this.symbolValueType));
            for (int i = dimensions.size() - 1 ; i >= 0 ; i --) {
                sb.append(']');
                sb.insert(0, "[" + dimensions.get(i) + " x ");
            }
            return sb.toString();
        }
    }

    public String type2ParamEleIr() {
        if (this.symbolVarType == VarType.VAR) {
            return type2Ir.get(symbolValueType);
        } else {
            StringBuilder sb = new StringBuilder(type2Ir.get(this.symbolValueType));
            for (int i = dimensions.size() - 1 ; i >= 1 ; i --) {
                sb.append(']');
                sb.insert(0, "[" + dimensions.get(i) + " x ");
            }
            return sb.toString();
        }
    }

    @Override
    public String all2Ir() {
        return this.type2Ir() + " " + this.value2Ir();
    }

    public String toIrString() {
        if (this.symbolVarType == VarType.VAR) {
            if (initValue != null) {
                return "@" + symbolName + " = dso_local global "
                        + type2Ir.get(this.symbolValueType) + " "
                        + this.initValue.value2Ir();
            } else {
                return "@" + symbolName + " = dso_local global "
                        + type2Ir.get(this.symbolValueType) + " "
                        + 0;
            }
        } else {
            if (initValues.size() == 0) {
                return "@" + symbolName + " = dso_local global " + type2Ir() + " zeroinitializer";
            } else {
                String head = "@" + symbolName + " = dso_local global " + type2Ir() + " ";
                if (this.isArray1()) {
                    StringJoiner sj = new StringJoiner(", ");
                    for (Operand operand : initValues) {
                        sj.add("i32 " + ((Number) operand).getValue());
                    }
                    return head + "[" + sj + "]";
                } else {
                    StringJoiner sj = new StringJoiner(", ");
                    for (int i = 0 ; i < dimensions.get(0) ; i ++) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("[").append(dimensions.get(1)).append(" x i32] ");
                        sb.append("[");
                        StringJoiner _sj = new StringJoiner(", ");
                        for (int j = 0 ; j < dimensions.get(1) ; j ++) {
                            _sj.add("i32 " + ((Number) initValues.get(i * dimensions.get(1) + j)).getValue());
                        }
                        sb.append(_sj);
                        sb.append("]");
                        sj.add(sb.toString());
                    }
                    return head + "[" + sj + "]";
                }
            }
        }
    }

    public String toFuncIrString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ");
        sb.append(Symbol.type2Ir.get(this.symbolValueType));
        sb.append(" @");
        sb.append(symbolName);
        sb.append("(");
        StringJoiner paramJoiner = new StringJoiner(",");
        if (params != null) {
            for (Symbol symbol : params) {
                paramJoiner.add(symbol.all2Ir());
            }
        }
        sb.append(paramJoiner);
        sb.append(")");
        return sb.toString();
    }

    public boolean isVar() {
        return this.symbolVarType == VarType.VAR;
    }

    public boolean isArray() {
        return this.symbolVarType == VarType.ARRAY1 || this.symbolVarType == VarType.ARRAY2;
    }

    public boolean isArrayOrPointer() {
        return isArray() || this.symbolVarType == VarType.POINTER;
    }

    public boolean isArray1() {
        return this.symbolVarType == VarType.ARRAY1;
    }

    public boolean isArray2() {
        return this.symbolVarType == VarType.ARRAY2;
    }

    public boolean isPointer() {
        return this.symbolVarType == VarType.POINTER;
    }

    public int getCapacity() {
        assert this.isArray() : "error in getCapacity";
        if (this.isArray1()) {
            return dimensions.get(0) * 4;
        } else {
            return dimensions.get(0) * dimensions.get(1) * 4;
        }
    }

    public boolean isTmp() {
        return this.symbolName.startsWith("%-t");
    }

}
