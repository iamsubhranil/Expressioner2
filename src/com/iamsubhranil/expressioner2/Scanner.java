package com.iamsubhranil.expressioner2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import static com.iamsubhranil.expressioner2.TokenType.*;

public class Scanner {

    private final List<Token> tokens;
    private final String source;
    private int current = 0, start = 0;
    public static final Map<String, TokenType> functions = new HashMap<>();

    static {
        functions.put("sin", SIN);
        functions.put("cos", COS);
        functions.put("tan", TAN);
        functions.put("cosec", COSEC);
        functions.put("sec", SEC);
        functions.put("cot", COT);
        functions.put("sinh", SINH);
        functions.put("cosh", COSH);
        functions.put("tanh", TANH);
        functions.put("log", LOG);
        functions.put("log10", LOG10);
        functions.put("sqrt", SQRT);
        functions.put("exp", EXP);
        functions.put("asin", ASIN);
        functions.put("asinh", ASINH);
        functions.put("acos", ACOS);
        functions.put("acosh", ACOSH);
        functions.put("atan", ATAN);
        functions.put("atanh", ATANH);
    }

    public Scanner(String input) {
        tokens = new ArrayList<>();
        source = input;
    }

    public List<Token> getTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "EOF", current));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_CURL);
                break;
            case '[':
                addToken(TokenType.LEFT_BRACE);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case ']':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_CURL);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '/':
                addToken(TokenType.SLASH);
                break;
            case '%':
                addToken(TokenType.PERCEN);
                break;
            case '^':
                addToken(TokenType.CARET);
                break;
            case '!':
                addToken(TokenType.BANG);
                break;
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                // Ignore whitespace.
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Expressioner.error(start, "Unrecognized character "+c+"!");
                    //error
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        TokenType t = functions.get(source.substring(start, current));
        addToken(t==null?TokenType.IDENTIFIER:t);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();

            addToken(TokenType.NUMBER,
                    new BigDecimal(source.substring(start, current)));
        }
        else{
            addToken(TokenType.NUMBER, new BigInteger(source.substring(start, current)));
        }
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, start));
    }

    private void addToken(TokenType type, Object literal){
        tokens.add(new Token(type, literal, start));
    }
}