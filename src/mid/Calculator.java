package mid;

import frontend.Node;
import frontend.Symbol;
import mid.ir.Number;

public class Calculator {

    public static int calConstExp(Node node) {
        // ConstExp → AddExp
        return calAddExp(node.getSubNodesByType(Node.NodeType.AddExp).get(0));
    }

    public static int calExp(Node node) {
        // Exp → AddExp
        return calAddExp(node.getSubNodesByType(Node.NodeType.AddExp).get(0));
    }

    private static int calAddExp(Node node) {
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        if (node.nodeList.size() == 1) {
            return calMulExp(node.getSubNodesByType(Node.NodeType.MulExp).get(0));
        } else {
            int left = calAddExp(node.getSubNodesByType(Node.NodeType.AddExp).get(0));
            int right = calMulExp(node.getSubNodesByType(Node.NodeType.MulExp).get(0));
            String op = node.nodeList.get(1).name;
            return calculate(left, right, op);
        }
    }

    private static int calMulExp(Node node) {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        if (node.nodeList.size() == 1) {
            return calUnaryExp(node.getSubNodesByType(Node.NodeType.UnaryExp).get(0));
        } else {
            int left = calMulExp(node.getSubNodesByType(Node.NodeType.MulExp).get(0));
            int right = calUnaryExp(node.getSubNodesByType(Node.NodeType.UnaryExp).get(0));
            String op = node.nodeList.get(1).name;
            return calculate(left, right, op);
        }
    }

    private static int calUnaryExp(Node node) {
        // UnaryExp → PrimaryExp | UnaryOp UnaryExp
        if (node.subNodesContain(Node.NodeType.PrimaryExp)) {
            return calPrimaryExp(node.getSubNodesByType(Node.NodeType.PrimaryExp).get(0));
        } else {
            int right = calUnaryExp(node.getSubNodesByType(Node.NodeType.UnaryExp).get(0));
            return calculate(right, node.getSubNodesByType(Node.NodeType.UnaryOp).get(0).nodeList.get(0).name);
        }
    }

    private static int calPrimaryExp(Node node) {
        // PrimaryExp → '(' Exp ')' | LVal | Number
        if (node.subNodesContain(Node.NodeType.Exp)) {
            return calExp(node.getSubNodesByType(Node.NodeType.Exp).get(0));
        } else if (node.subNodesContain(Node.NodeType.Number)) {
            return Integer.parseInt(node.getSubNodesByType(Node.NodeType.Number).get(0).nodeList.get(0).name);
        } else {
            return calLval(node.getSubNodesByType(Node.NodeType.LVal).get(0));
        }
    }

    private static int calLval(Node node) {
        // LVal → Ident {'[' Exp ']'}
        Symbol symbol = node.getSymbolTable().getVisableSymbol(node.getSubNodesByType(Node.NodeType.IDENFR).get(0).name);
        assert symbol.isGlobal || symbol.isConst : "error in calLVal, symbol is not global ot const";
        if (symbol.isVar()) {
            return symbol.globalValue;
        }
        if (symbol.isArrayOrPointer()) {
            if (symbol.isArray1()) {
                Node exp1 = node.getSubNodesByType(Node.NodeType.Exp).get(0);
                return ((Number) symbol.initValues.get(calExp(exp1))).getValue();
            }
            if (symbol.isArray2()) {
                Node exp1 = node.getSubNodesByType(Node.NodeType.Exp).get(0);
                Node exp2 = node.getSubNodesByType(Node.NodeType.Exp).get(1);
                return ((Number) symbol.initValues.get(calExp(exp1) * symbol.dimensions.get(1) + calExp(exp2))).getValue();
            }
        }
        assert false : "error in calLVal";
        return 0;
    }

    private static int calculate(int leftInt, int rightInt, String operator) {
        return switch (operator) {
            case "+" -> leftInt + rightInt;
            case "-" -> leftInt - rightInt;
            case "*" -> leftInt * rightInt;
            case "/" -> leftInt / rightInt;
            case "%" -> leftInt % rightInt;
            case "==" -> (leftInt == rightInt) ? 1 : 0;
            case "!=" -> (leftInt != rightInt) ? 1 : 0;
            case ">" -> (leftInt > rightInt) ? 1 : 0;
            case ">=" -> (leftInt >= rightInt) ? 1 : 0;
            case "<" -> (leftInt < rightInt) ? 1 : 0;
            case "<=" -> (leftInt <= rightInt) ? 1 : 0;
            default -> 0;
        };
    }

    private static int calculate(int rightInt, String operator) {
        return switch (operator) {
            case "+" -> + rightInt;
            case "-" -> - rightInt;
            case "!" -> rightInt > 0 ? 0 : 1;
            default -> 0;
        };
    }

}
