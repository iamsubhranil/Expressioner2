package com.iamsubhranil.expressioner2;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class Expressioner {

    private static String source = "";
    static MathContext mathContext = MathContext.DECIMAL128;

    private static Object getArg(String param, Iterator<String> argIterator, boolean allowString){
        if(!argIterator.hasNext())
            return null;
        while(argIterator.hasNext()){
            String arg = argIterator.next();
            if(arg.startsWith("--"+param) || arg.startsWith("-"+param.charAt(0))){
                String pre;
                if(arg.contains("="))
                    pre = arg.replace("--"+param+"=","").replace("-"+param.charAt(0)+"=","");
                else{
                    try {
                        pre = argIterator.next();
                    }
                    catch (NoSuchElementException nsee){
                        System.err.println("[Warning] Incomplete "+param.replace("_"," ")+
                                " argument! Using default "+param.replace("_"," ")+"!");
                        return null;
                    }
                }
                try{
                    return Integer.valueOf(pre);
                }
                catch (NumberFormatException nfe){
                    if(!allowString)
                        System.err.println("[Warning] Bad "+param+" argument! Using default "+param+"!");
                    return pre;
                }
            }
        }
        return null;
    }

    public static void main(String [] args){
        if(args.length >= 1 && args.length <= 4 ) {
            int precision = mathContext.getPrecision();
            RoundingMode mode = mathContext.getRoundingMode();
            List<String> as = new ArrayList<>();
            Collections.addAll(as, args);
            Object tp = getArg("precision", as.iterator(), false);
            if(tp !=null && tp instanceof Integer) {
                if((int) tp <= 0)
                    System.err.println("[Warning] Bad precision argument! Using default");
                else
                    precision = (int) tp;
            }
            Object rMode = getArg("rounding_mode", as.iterator(), true);
            if(rMode != null && rMode instanceof String){
                try{
                    mode = RoundingMode.valueOf((String)rMode);
                }
                catch (IllegalArgumentException iae){
                    System.err.println("[Warning] Bad rounding mode argument! Using default!");
                }
            }
            else if(rMode != null){
                try {
                    mode = RoundingMode.valueOf((int)rMode);
                }
                catch (IllegalArgumentException iae){
                    System.err.println("[Warning] Bad rounding mode argument! Using default!");
                }
            }
            System.out.println("[Info] Using precision "+precision+" and mode "+
                    mode.toString().replace("_"," ").toLowerCase()
                    +"!");
            mathContext = new MathContext(precision, mode);
        }
        else if(args.length > 4){
            System.err.println("[Warning] Excess arguments! Ignoring all!");
        }
        System.out.print("[Input] Enter the expression : ");
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
            System.out.println("[Output] Result : "+solver.solve().toString());
        }
        catch (ArithmeticException ar){
            System.err.println("[Error] "+ar.getMessage());
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
