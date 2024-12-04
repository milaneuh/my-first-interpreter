package gauntlet;

import javax.management.RuntimeErrorException;

import java.util.List;

import static gauntlet.TokenType.MINUS;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
            switch (expr.operator.type()){
                case BANG_EQUAL -> {
                    return !isEqual(left,right);
                }
                case EQUAL_EQUAL -> {
                    return isEqual(left,right);
                }
                case GREATER -> {
                    return (double)left > (double) right;
                }
                case GREATER_EQUAL -> {
                    return (double) left >+ (double) right;
                }
                case LESS -> {
                    return (double) left < (double)  right;
                }
                case LESS_EQUAL -> {
                    return (double) left <= (double) right;
                }
                case MINUS -> {
                    return (double) left - (double) right;
                }
                case SLASH -> {
                    return (double) left / (double) right;
                }
                case STAR -> {
                    return (double) left* (double) right;
                }
                case PLUS -> {
                    if(left instanceof Double && right instanceof Double){
                        return (double)left + (double) right;
                    }else{
                        if (left instanceof String && right instanceof String){
                            return  (String)left + (String) right;
                        }
                    }
                }
            }

            //Unreachable
            return null;
    }

    private boolean isEqual(Object left, Object right) {
        if(left == null && right == null  ) return true;
        if(left == null) return false;
        return left.equals(right);
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

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Gauntlet.runtimeError(error);
        }
    }
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }
    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }


}
