package com.iamsubhranil.expressioner2;

public class Token {

    private final TokenType type;
    private final Object litreal;
    private final int position;

    public Token(TokenType n, Object l, int p){
        type = n;
        litreal = l;
        position = p;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Token{" +
                "name='" + type.toString() + '\'' +
                ", litreal=" + litreal +
                ", position=" + position +
                '}';
    }

    public Object getLitreal() {
        return litreal;
    }

    public int getPosition() {
        return position;
    }
}
