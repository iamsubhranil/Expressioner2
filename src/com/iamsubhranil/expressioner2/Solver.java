package com.iamsubhranil.expressioner2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Scanner;

public class Solver implements Expr.Visitor<Number> {

    private final Expr expr;
    private final HashMap<String, Number> environemnt = new HashMap<>();


    public Solver(Expr e){
        expr = e;
    }

    public Number solve(){
        return expr.accept(this);
    }

    @Override
    public Number visitBinaryExpr(Expr.Binary expr) {
        Number left = expr.left.accept(this);
        Number right = expr.right.accept(this);
        if(left instanceof BigDecimal || right instanceof BigDecimal || expr.operator.getType()==TokenType.SLASH){
            BigDecimal l = decValue(left), r = decValue(right);
            switch (expr.operator.getType()){
                case MINUS:
                    return l.subtract(r);
                case PLUS:
                    return l.add(r);
                case STAR:
                    return l.multiply(r);
                case SLASH:
                    try{
                        return l.divide(r);
                    }
                    catch (ArithmeticException ae) {
                        return l.divide(r, MathContext.DECIMAL128);
                    }
                case PERCEN:
                    return l.remainder(r);
                case CARET:
                    if(right instanceof BigDecimal){
                        Expressioner.error(expr.operator, "Exponent must be integer!");
                        return null;
                    }
                    try {
                        return l.pow(r.intValueExact());
                    }
                    catch (ArithmeticException ae){
                        Expressioner.fatal(expr.operator, "Number out of range for '^'(power of) operation!");
                        return null;
                    }
            }

        }
        else{
            BigInteger l = (BigInteger)left, r = (BigInteger)right;
            switch (expr.operator.getType()){
                case MINUS:
                    return l.subtract(r);
                case PLUS:
                    return l.add(r);
                case STAR:
                    return l.multiply(r);
                case PERCEN:
                    return l.remainder(r);
                case CARET:
                    try {
                        return l.pow(r.intValueExact());
                    }
                    catch (ArithmeticException ae){
                        Expressioner.fatal(expr.operator, "Exponent out of range for '^'(power of) operation!");
                        return null;
                    }
            }
        }
        return null;
    }

    private BigDecimal decValue(Number n){
        if(n instanceof BigDecimal)
            return (BigDecimal)n;
        else
            return new BigDecimal((BigInteger)n);
    }

    @Override
    public Number visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public Number visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Number visitUnaryExpr(Expr.Unary expr) {
        Token op = expr.operator;
        Number result = expr.right.accept(this);
        if(expr.operator.getType() == TokenType.MINUS){
            if(result instanceof BigDecimal)
                return ((BigDecimal) result).multiply(BigDecimal.valueOf(-1.0));
            else
                return ((BigInteger) result).multiply(BigInteger.valueOf(-1L));
        }
        else{
            if(result instanceof BigDecimal)
                Expressioner.fatal(op, "'!' can't be applied over floating point numbers!");
            else{
                BigInteger i = (BigInteger) result;
                if(i.intValue() == 0 || i.intValue()==1)
                    return BigInteger.valueOf(1);
                BigInteger t = BigInteger.valueOf(2), res = i;
                while(t.compareTo(i) < 0){
                    res = res.multiply(t);
                    t = t.add(BigInteger.valueOf(1));
                }
                return res;
            }

        }
        return null;
    }

    @Override
    public Number visitVariableExpr(Expr.Variable expr) {
        if(environemnt.containsKey(expr.name.getLitreal()))
            return environemnt.get(expr.name.getLitreal());

        System.out.println("Enter the value of '"+expr.name.getLitreal()+"' : ");
        Scanner s = new Scanner(System.in);
        while(!s.hasNextBigDecimal()  && !s.hasNextBigInteger()){
            System.err.println("[Error] Enter a numeric value!");
            s.next();
        }
        Number n;
        if(s.hasNextBigInteger())
            n = s.nextBigInteger();
        else
            n = s.nextBigDecimal();
        environemnt.put(expr.name.getLitreal().toString(), n);
        return n;
    }

    private double getResult(TokenType t, double val){
        switch (t){
            case SIN:
                return Math.sin(val);
            case COS:
                return Math.cos(val);
            case TAN:
                return Math.tan(val);
            case SEC:
                return 1.0/Math.cos(val);
            case COSEC:
                return 1.0/Math.sin(val);
            case COT:
                return 1.0/Math.tan(val);
            case SINH:
                return Math.sinh(val);
            case COSH:
                return Math.cosh(val);
            case TANH:
                return Math.tanh(val);
            case LOG:
                return Math.log(val);
            case LOG10:
                return Math.log10(val);
            case SQRT:
                return Math.sqrt(val);
        }
        return Double.NaN;
    }

    @Override
    public Number visitFunctionExpr(Expr.Function expr) {
        Token f = expr.name;
        double val = decValue(expr.argument.accept(this)).doubleValue();
        return new BigDecimal(getResult(f.getType(), val));
    }
}
