package com.iamsubhranil.expressioner2;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Scanner;

public class Solver implements Expr.Visitor<Number> {
    private final HashMap<String, Number> environment = new HashMap<>();

    public Solver(){ }

    public Number solve(Expr e){
        return e.accept(this);
    }

    @Override
    public Number visitBinaryExpr(Expr.Binary expr) {
        Expressioner.debug("[Solver] Solving binary");
        Number left = expr.left.accept(this);
        Number right = expr.right.accept(this);
        if(left instanceof BigDecimal || right instanceof BigDecimal || expr.operator.getType()==TokenType.SLASH){
            BigDecimal l = decValue(left), r = decValue(right);
            Expressioner.debug("[Solver] Upscaling to BigDecimal");
            switch (expr.operator.getType()){
                case MINUS:
                    return l.subtract(r, Expressioner.mathContext);
                case PLUS:
                    return l.add(r, Expressioner.mathContext);
                case STAR:
                    return l.multiply(r, Expressioner.mathContext);
                case SLASH:
                    return l.divide(r, Expressioner.mathContext);
                case PERCEN:
                    return l.remainder(r, Expressioner.mathContext);
                case CARET:
                    if(right instanceof BigDecimal){
                        Expressioner.error(expr.operator, "Exponent must be integer!");
                        return null;
                    }
                    try {
                        return BigDecimalMath.pow(l, r, Expressioner.mathContext);
                    }
                    catch (ArithmeticException ae){
                        Expressioner.fatal(expr.operator, "Number out of range for '^'(power of) operation!");
                        return null;
                    }
            }

        }
        else{
            Expressioner.debug("[Solver] Performing BigInteger operation");
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
        Expressioner.debug("[Solver] Solving grouping expression");
        return expr.expression.accept(this);
    }

    @Override
    public Number visitLiteralExpr(Expr.Literal expr) {
        Expressioner.debug("[Solver] Returning value literal value "+expr.value.toString());
        return expr.value;
    }

    @Override
    public Number visitUnaryExpr(Expr.Unary expr) {
        Expressioner.debug("[Solver] Solving unary expression");
        Token op = expr.operator;
        Number result = expr.right.accept(this);
        if(expr.operator.getType() == TokenType.MINUS){
            if(result instanceof BigDecimal)
                return ((BigDecimal) result).multiply(BigDecimal.valueOf(-1.0), Expressioner.mathContext);
            else
                return ((BigInteger) result).multiply(BigInteger.valueOf(-1L));
        }
        else{
            if(result instanceof BigDecimal)
                Expressioner.fatal(op, "'!' can't be applied over floating point numbers!");
            else{
                return BigDecimalMath.factorial(result.intValue());
            }

        }
        return null;
    }

    private boolean match(Object toMatch, String ...options){
        for(String opt : options){
            if(opt.equals(toMatch))
                return true;
        }
        return false;
    }

    private BigDecimal getConstant(String name){
        Expressioner.debug("[Solver] Returning constant value for "+name);
        if(name.equals("PI"))
            return BigDecimalMath.pi(Expressioner.mathContext);
        else if(name.equals("E"))
            return BigDecimalMath.e(Expressioner.mathContext);

        return BigDecimal.ZERO;
    }

    @Override
    public Number visitVariableExpr(Expr.Variable expr) {
        Expressioner.debug("[Solver] Solving variable");
        if(match(expr.name.getLiteral(), "PI", "E"))
            return getConstant((String)expr.name.getLiteral());

        if(environment.containsKey(expr.name.getLiteral()))
            return environment.get(expr.name.getLiteral());

        System.out.print("[Input] Enter the value of '"+expr.name.getLiteral()+"' : ");
        Scanner s = new Scanner(System.in);
        while(!s.hasNextBigDecimal()  && !s.hasNextBigInteger()){
            Expressioner.warning("[Error] Enter a numeric value!");
            System.out.print("[Input] Enter the value of '"+expr.name.getLiteral()+"' : ");
            s.next();
        }
        Number n;
        if(s.hasNextBigInteger())
            n = s.nextBigInteger();
        else
            n = s.nextBigDecimal();
        environment.put(expr.name.getLiteral().toString(), n);
        return n;
    }

    private BigDecimal getResult(TokenType t, BigDecimal val){
        switch (t){
            case SIN:
                return BigDecimalMath.sin(val, Expressioner.mathContext);
            case COS:
                return BigDecimalMath.cos(val, Expressioner.mathContext);
            case TAN:
                return BigDecimalMath.tan(val, Expressioner.mathContext);
            case SEC:
                return new BigDecimal(1.0).divide(BigDecimalMath.cos(val, Expressioner.mathContext), Expressioner.mathContext);
            case COSEC:
                return new BigDecimal(1.0).divide(BigDecimalMath.sin(val, Expressioner.mathContext), Expressioner.mathContext);
            case COT:
                return new BigDecimal(1.0).divide(BigDecimalMath.tan(val, Expressioner.mathContext), Expressioner.mathContext);
            case SINH:
                return BigDecimalMath.sinh(val, Expressioner.mathContext);
            case COSH:
                return BigDecimalMath.cosh(val, Expressioner.mathContext);
            case TANH:
                return BigDecimalMath.tanh(val, Expressioner.mathContext);
            case LOG:
                return BigDecimalMath.log(val, Expressioner.mathContext);
            case LOG10:
                return BigDecimalMath.log10(val, Expressioner.mathContext);
            case SQRT:
                return BigDecimalMath.sqrt(val, Expressioner.mathContext);
            case EXP:
                return BigDecimalMath.exp(val, Expressioner.mathContext);
            case ASIN:
                return BigDecimalMath.asin(val, Expressioner.mathContext);
            case ASINH:
                return BigDecimalMath.asinh(val, Expressioner.mathContext);
            case ATAN:
                return BigDecimalMath.atan(val, Expressioner.mathContext);
            case ATANH:
                return BigDecimalMath.atanh(val, Expressioner.mathContext);
            case ACOS:
                return BigDecimalMath.acos(val, Expressioner.mathContext);
            case ACOSH:
                return BigDecimalMath.acosh(val, Expressioner.mathContext);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Number visitFunctionExpr(Expr.Function expr) {
        Expressioner.debug("[Solver] \\|/ Solving function "+expr.name.getLiteral());
        Token f = expr.name;
        BigDecimal val = decValue(expr.argument.accept(this));
        Expressioner.debug("[Solver] /|\\ Solved function "+expr.name.getLiteral());
        return getResult(f.getType(), val);
    }
}
