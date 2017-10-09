package com.iamsubhranil.expressioner2;

import java.util.List;

public class Expressioner {

    private static String source = "";

    public static void main(String [] args){
        System.out.println("Enter the expression : ");
        java.util.Scanner reader = new java.util.Scanner(System.in);
        source = reader.nextLine();
        Scanner s = new Scanner(source);
        List<Token> tokenList = s.getTokens();
        //tokenList.forEach(token -> System.out.print(token.getType()+" "));
        Parser parser = new Parser(tokenList);
        try {
            Expr e = parser.parse();
           // AstPrinter ap = new AstPrinter();
           // System.out.println(ap.print(e));
            Solver solver = new Solver(e);
            System.out.println("Result : "+solver.solve().toString());
        }
        catch (ArithmeticException ar){
            System.err.println(ar.getMessage());
        }
        catch (Exception ignored){
        }
    }

    public static void error(int position, String message){
        System.err.println("[Error:"+position+"] "+source);
        System.err.print("[Error:"+position+"] ");
        for(int i = 0;i < position;i++)
            System.err.print(" ");
        System.err.print("^\n");
        System.err.println("[Error:"+position+"] "+message);
    }

    public static void error(Token token, String message){
        error(token.getPosition(), message);
    }

    public static void fatal(int position, String message){
        error(position, message);
        throw new RuntimeException();
    }

    public static void fatal(Token token, String message){
        fatal(token.getPosition(), message);
    }
}
