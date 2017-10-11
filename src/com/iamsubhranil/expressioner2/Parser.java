package com.iamsubhranil.expressioner2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import static com.iamsubhranil.expressioner2.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        Expressioner.debug("[Parser] Generating expression");
        return expression();
    }

    private Expr expression() {
        Expr mother = expr();
        while ((check(LEFT_PAREN) || check(LEFT_BRACE) || check(LEFT_CURL))
            &&!isAtEnd()) {
            Expressioner.debug("[Parser] Found brace without operator! Multiplying implicitly!");
            Expr e = expr();
            mother = new Expr.Binary(mother, token(STAR, "*"), e);
        }
        if(!isAtEnd())
            Expressioner.fatal(peek(), "Expected operator or brace before '"+peek().getLiteral()+"' !");
        return mother;
    }

    private Expr expr() {
        return modulo();
    }

    private Token token(TokenType type, Object lit) {
        return new Token(type, lit, 0);
    }

    private Expr modulo() {
        Expressioner.debug("[Parser] Current token : "+tokens.get(current));
        Expressioner.debug("[Parser] \\|/ modulo()");
        Expr expr = addition();

        while (match(PERCEN)) {
            Expressioner.debug("[Parser] = modulo() [Found %]");
            Token operator = previous();
            Expr right = addition();
            Expressioner.debug("[Parser] /|\\ modulo()");
            expr = new Expr.Binary(expr, operator, right);
        }

        Expressioner.debug("[Parser] /|\\ modulo()");
        return expr;
    }

    private Expr addition() {
        //   System.out.println("[Addition]Current token "+tokens.get(current));
        Expressioner.debug("[Parser] \\|/ addition()");
        Expr expr = multiplication();

        while (match(MINUS, PLUS)) {
            Expressioner.debug("[Parser] = addition() [Found "+previous().getLiteral()+"]");
            Token operator = previous();
            Expr right = multiplication();
            Expressioner.debug("[Parser] /|\\ addition()");
            expr = new Expr.Binary(expr, operator, right);
        }

        Expressioner.debug("[Parser] /|\\ addition()");
        return expr;
    }

    private Expr multiplication() {
        Expressioner.debug("[Parser] \\|/ multiplication()");
        //      System.out.println("[Multiplication]Current token "+tokens.get(current));
        Expr expr = tothepower();

        while (match(SLASH, STAR)) {
            Expressioner.debug("[Parser] = multiplication() [Found "+previous().getLiteral()+"]");
            Token operator = previous();
            Expr right = tothepower();
            Expressioner.debug("[Parser] /|\\ multiplication()");
            expr = new Expr.Binary(expr, operator, right);
        }

        Expressioner.debug("[Parser] /|\\ multiplication()");
        return expr;
    }

    private Expr tothepower() {
        Expressioner.debug("[Parser] \\|/ tothepower()");
        //   System.out.println("[Tothepower]Current token "+tokens.get(current));
        Expr expr = unary();
        while (match(CARET)) {
            Token operator = previous();
            Expressioner.debug("[Parser] = tothepower() [Found ^]");
            Expr right = unary();
            Expressioner.debug("[Parser] /|\\ tothepower()");
            expr = new Expr.Binary(expr, operator, right);
        }
        Expressioner.debug("[Parser] /|\\ tothepower()");
        return expr;
    }

    private Expr unary() {
        Expressioner.debug("[Parser] \\|/ unary()");
        //   System.out.println("[Unary]Current token "+tokens.get(current));
        if (match(MINUS)) {
            Token operator = previous();
            Expressioner.debug("[Parser] = unary() [Found -]");
            Expr right = unary();
            Expressioner.debug("[Parser] /|\\ unary()");
            return new Expr.Unary(operator, right);
        }
        Expr e = primary();
        if (match(BANG)) {
            Expressioner.debug("[Parser] = unary() [Found !]");
            Token operator = previous();
            Expressioner.debug("[Parser] /|\\ unary()");
            return new Expr.Unary(operator, e);
        }
        Expressioner.debug("[Parser] /|\\ unary()");
        return e;

    }

    private Expr getGroupingExpr(TokenType end){
        Expr expr = null;
        Expressioner.debug("[Parser] \\|/ group()");
        while(!check(end)) {
            if(expr == null)
                expr = expr();
            else
                expr = new Expr.Binary(expr, token(STAR, "*"), expr());
        }
        consume(end, "Expected "+end+" after expression!");
        Expressioner.debug("[Parser] /|\\ group()");
        return new Expr.Grouping(expr);
    }

    private Expr primary() {

        //    System.out.println("[Primary]Current token "+tokens.get(current));
        Expressioner.debug("[Parser] \\|/ primary()");
        if (match(NUMBER)) {
            Expressioner.debug("[Parser] = primary() [Found "+previous()+"]");
            Expressioner.debug("[Parser] /|\\ primary()");
            if (previous().getLiteral() instanceof BigDecimal)
                return new Expr.Literal((BigDecimal) previous().getLiteral());
            else
                return new Expr.Literal((BigInteger) previous().getLiteral());
        }

        if (match(Scanner.functions.values())) {
            Expr e = function();
            Expressioner.debug("[Parser] /|\\ primary()");
            return e;
        }

        if (match(IDENTIFIER)) {
            Expressioner.debug("[Parser] Found identifer "+previous()+"!");
            Expressioner.debug("[Parser] /|\\ primary()");
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr e = getGroupingExpr(RIGHT_PAREN);
            Expressioner.debug("[Parser] /|\\ primary()");
            return e;
        }

        if (match(LEFT_BRACE)) {
            Expr e = getGroupingExpr(RIGHT_BRACE);
            Expressioner.debug("[Parser] /|\\ primary()");
            return e;
        }

        if (match(LEFT_CURL)) {
            Expr e = getGroupingExpr(RIGHT_CURL);
            Expressioner.debug("[Parser] /|\\ primary()");
            return e;
        }

        if(match(RIGHT_PAREN, RIGHT_BRACE, RIGHT_CURL))
            throw error(previous(), "Unexpected closing brace '"+previous().getLiteral()+"' !");

        throw error(peek(), "Unexpected end of expression!");
    }

    private boolean match(Collection<TokenType> c) {
        for (TokenType aC : c) {
            if (match(aC)) {
                return true;
            }
        }
        return false;
    }

    private Expr function() {
        Expressioner.debug("[Parser] \\|/ function()");
        //     System.out.println("[Function] Current token "+tokens.get(current));
        Token f = previous();
        consume(LEFT_PAREN, "Functions must proceed with '('");
        Expr arg = expr();
        consume(RIGHT_PAREN, "Functions must end with ')'");
        Expressioner.debug("[Parser] /|\\ function()");
        return new Expr.Function(f, arg);
    }
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType tokenType) {
        if (isAtEnd()) return false;
        return peek().getType() == tokenType;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Expressioner.error(token, message);
        return new ParseError();
    }
}
