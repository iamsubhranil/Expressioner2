package com.iamsubhranil.expressioner2;

public class ASTInPrinter implements Expr.Visitor<String>{

    public String toInFix(Expr e){
        return e.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this)+" "+expr.operator.getLiteral()+" "+expr.right.accept(this);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return "("+expr.expression.accept(this)+")";
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.operator.getLiteral()+" "+expr.right.accept(this);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return (String)expr.name.getLiteral();
    }

    @Override
    public String visitFunctionExpr(Expr.Function expr) {
        return expr.name.getLiteral()+"("+expr.argument.accept(this)+")";
    }
}
