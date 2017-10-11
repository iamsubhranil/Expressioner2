package com.iamsubhranil.expressioner2;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ASTWriter implements Expr.Visitor<Void>{

    private final ObjectOutputStream outputStream;

    public static final int LITERAL = 0xAA;
    public static final int VARIABLE = 0xAB;
    public static final int UNARY = 0xAC;
    public static final int BINARY = 0xAD;
    public static final int GROUPING = 0xAE;
    public static final int FUNCTION = 0xAF;

    public ASTWriter(ObjectOutputStream o){
        outputStream = o;
    }

    public void write(Expr e){
        e.accept(this);
    }

    public void logException(Exception e){
        try{
            outputStream.close();
        }
        catch (IOException ie){}
        throw new RuntimeException(e.getMessage());
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr){
        try {
            outputStream.writeInt(LITERAL);
         //   System.out.println("[Literal] Wrote magic "+LITERAL);
            outputStream.writeObject(expr.value);
         //   System.out.println("[Literal] Wrote literal");
        }
        catch (IOException ie){
            logException(ie);
        }
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        try {
            outputStream.writeInt(VARIABLE);
         //   System.out.println("[Variable] Wrote magic "+VARIABLE);
            outputStream.writeObject(expr.name);
          //  System.out.println("[Variable] Wrote Variable");
        }
        catch (IOException ie){
            logException(ie);
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        try {
            outputStream.writeInt(UNARY);
          //  System.out.println("[Unary] Wrote magic "+ UNARY);
            outputStream.writeObject(expr.operator);
            expr.right.accept(this);
          //  System.out.println("[Unary] Wrote unary");
        }
        catch (IOException ie){
            logException(ie);
        }
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        try {
            outputStream.writeInt(BINARY);
          //  System.out.println("[Binary] Wrote magic "+BINARY);
            expr.left.accept(this);
            outputStream.writeObject(expr.operator);
            expr.right.accept(this);
         //   System.out.println("[Binary] Wrote binary");
        }
        catch (IOException ie){
            logException(ie);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        try {
            outputStream.writeInt(GROUPING);
           // System.out.println("[Grouping] Wrote magic "+GROUPING);
            expr.expression.accept(this);
          //  System.out.println("[Grouping] Wrote grouping");
        } catch (IOException e) {
            logException(e);
        }
        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        try {
            outputStream.writeInt(FUNCTION);
          //  System.out.println("[Function] Wrote magic "+FUNCTION);
            outputStream.writeObject(expr.name);
            expr.argument.accept(this);
          //  System.out.println("[Function] Wrote function");
        }
        catch (IOException ie){
            logException(ie);
        }
        return null;
    }
}
