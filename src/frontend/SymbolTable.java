package frontend;

import java.util.HashMap;

public class SymbolTable {

    public SymbolTable father = null;

    public HashMap<String, Symbol> table =  new HashMap<>();

    public void addSymbol (String symbolName, Symbol symbol) {
        table.put(symbolName, symbol);
    }

    public boolean containsSymbol (String symbolName) {
        return getVisableSymbol(symbolName) != null;
    }

    public Symbol getVisableSymbol (String symbolName) {
        SymbolTable curSymbolTable = this;
        while (curSymbolTable != null) {
            if (curSymbolTable.table.containsKey(symbolName)) {
                if (Symbol.careDefined) {
                    if (curSymbolTable.table.get(symbolName).isDefined) {
                        return curSymbolTable.table.get(symbolName);
                    }
                } else {
                    return curSymbolTable.table.get(symbolName);
                }
            }
            curSymbolTable = curSymbolTable.father;
        }
        return null;
    }

    public void setSymbolDefined(String symbolName) {
        SymbolTable curSymbolTable = this;
        while(curSymbolTable != null) {
            if (curSymbolTable.table.containsKey(symbolName)) {
                Symbol symbol = curSymbolTable.table.get(symbolName);
                symbol.isDefined = true;
            }
            curSymbolTable = curSymbolTable.father;
        }
    }

}
