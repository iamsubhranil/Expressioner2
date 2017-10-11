package com.iamsubhranil.expressioner2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Token implements Serializable{

    private TokenType type;
    private Object literal;
    private int position;

    public Token(TokenType n, Object l, int p){
        type = n;
        literal = l;
        position = p;
    }

    protected Token(){
        type = TokenType.IDENTIFIER;
        literal = "";
        position = 0;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Token{" +
                "name='" + type.toString() + '\'' +
                ", literal=" + literal +
                ", position=" + position +
                '}';
    }

    public Object getLiteral() {
        return literal;
    }

    public int getPosition() {
        return position;
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
       // System.out.println("[Token] Out called for "+literal+"!");
        out.writeInt(type.ordinal());
        out.writeObject(literal);
        out.writeInt(position);
    }

    private void readObject(ObjectInputStream in) throws IOException{
       // System.out.println("[Token] In called!");
        type = TokenType.values()[in.readInt()];
        try {
            literal = in.readObject();
        } catch (ClassNotFoundException e) {
            Expressioner.fatal("Unable to read literal!");
        }
        position = in.readInt();
    }
}
