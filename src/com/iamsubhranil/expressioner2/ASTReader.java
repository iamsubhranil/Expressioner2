package com.iamsubhranil.expressioner2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ASTReader {

    private final ObjectInputStream ois;

    public ASTReader(ObjectInputStream o) throws IOException {
        ois = o;
    }

    public Expr read(){
        try{
            return defaultRead();
        }
        catch (EOFException ee){
          //  ee.printStackTrace();
            Expressioner.fatal("Unexpected end of binary!");
        }
        catch (IOException io){
          //  io.printStackTrace();
            Expressioner.fatal( "Unable to read input file!");
        }
        catch (ClassNotFoundException cnfe){
            Expressioner.fatal(cnfe.getMessage());
        }
        catch (ClassCastException cce){
            Expressioner.fatal("The binary is corrupted!");
        }
        return null;
    }

    private Expr defaultRead() throws IOException, ClassNotFoundException, ClassCastException, EOFException {
        int type = ois.readInt();
        switch (type){
            case ASTWriter.LITERAL:
                return readLiteral();
            case ASTWriter.VARIABLE:
                return readVariable();
            case ASTWriter.UNARY:
                return readUnary();
            case ASTWriter.BINARY:
                return readBinary();
            case ASTWriter.GROUPING:
                return readGrouping();
            case ASTWriter.FUNCTION:
                return readFunction();
            default:
                throw new IOException("Bad expression signature : "+type);
        }
    }

    private Expr readLiteral() throws IOException, ClassNotFoundException, ClassCastException {
        Expressioner.debug("[Reader] Reading literal!");
        return new Expr.Literal((Number)ois.readObject());
    }

    private Expr readVariable() throws IOException, ClassNotFoundException, ClassCastException {
        Expressioner.debug("[Reader] Reading variable!");
        return new Expr.Variable((Token)ois.readObject());
    }

    private Expr readUnary() throws IOException, ClassNotFoundException, ClassCastException {
        Expressioner.debug("[Reader] Reading unary!");
        return new Expr.Unary((Token)ois.readObject(), defaultRead());
    }

    private Expr readBinary() throws IOException, ClassNotFoundException, ClassCastException {
        Expressioner.debug("[Reader] Reading binary!");
        return new Expr.Binary(defaultRead(), (Token)ois.readObject(), defaultRead());
    }

    private Expr readGrouping() throws IOException, ClassNotFoundException, ClassCastException {
        Expressioner.debug("[Reader] Reading grouping!");
        return new Expr.Grouping(defaultRead());
    }

    private Expr readFunction() throws IOException, ClassNotFoundException, ClassCastException {
        Expressioner.debug("[Reader] Reading function!");
        return new Expr.Function((Token)ois.readObject(), defaultRead());
    }

}
