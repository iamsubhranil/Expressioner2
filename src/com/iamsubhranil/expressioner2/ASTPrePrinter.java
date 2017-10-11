package com.iamsubhranil.expressioner2;

// Creates an unambiguous, if ugly, string representation of AST nodes.
class ASTPrePrinter implements Expr.Visitor<String> {
    String toPrefix(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize((String)expr.operator.getLiteral(), expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize((String)expr.operator.getLiteral(), expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return (String)expr.name.getLiteral();
    }

    @Override
    public String visitFunctionExpr(Expr.Function expr) {
        return parenthesize((String)expr.name.getLiteral(), expr.argument);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        return builder.toString();
    }
}
