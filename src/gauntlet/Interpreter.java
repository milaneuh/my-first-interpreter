package gauntlet;

import static gauntlet.TokenType.MINUS;

public class Interpreter implements Expr.Visitor<Object> {
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
            switch (expr.operator.type()){
                case MINUS -> {
                    return (double) left - (double) right;
                }
                case SLASH -> {
                    return (double) left / (double) right;
                }
                case STAR -> {
                    return (double) left* (double) right;
                }
            }

            //Unreachable
            return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr) ;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value   ;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type()){
            case MINUS -> {
                return -(double)right;
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }

        return null;
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }
}
