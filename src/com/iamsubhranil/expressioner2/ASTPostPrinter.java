package com.iamsubhranil.expressioner2;

public class ASTPostPrinter implements Expr.Visitor<String>{

    public String toPostFix(Expr e){
        return e.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this)+" "+expr.right.accept(this)+" "+expr.operator.getLiteral();
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
        return expr.right.accept(this)+" "+expr.operator.getLiteral();
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return (String)expr.name.getLiteral();
    }

    @Override
    public String visitFunctionExpr(Expr.Function expr) {
        return expr.argument.accept(this)+" "+expr.name.getLiteral()+"()";
    }
}
