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
                return curSymbolTable.table.get(symbolName);
            }
            curSymbolTable = curSymbolTable.father;
        }
        return null;
    }

}
