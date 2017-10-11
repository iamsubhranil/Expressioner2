package com.iamsubhranil.expressioner2;

import java.io.*;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class Expressioner {

    private static String source = "";
    static MathContext mathContext = MathContext.DECIMAL128;

    private static Object getArg(String param, Iterator<String> argIterator, boolean allowString, boolean onlyCheckPresence){
        if(!argIterator.hasNext())
            return null;
        while(argIterator.hasNext()){
            String arg = argIterator.next();
            if(arg.startsWith("--"+param) || arg.startsWith("-"+param.charAt(0))){
                if(onlyCheckPresence)
                    return true;
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

    private static Expr load(String file){
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
           // System.out.println("Available : "+fis.available());
            ASTReader ar = new ASTReader(ois);
            Expr e = ar.read();
            ois.close();
            fis.close();
           // ASTPrePrinter ap = new ASTPrePrinter();
           // System.out.println(ap.print(e));
           // Solver solver = new Solver(e);
           // System.out.println("Result : "+solver.solve().toString());
            return e;
        } catch (IOException e) {
            fatal("[Error] "+e.getMessage());
        }
        return null;
    }

    private static Expr read(){
        java.util.Scanner reader = new java.util.Scanner(System.in);
        System.out.print("[Input] Enter the expression : ");
        source = reader.nextLine();
        Scanner s = new Scanner(source);
        List<Token> tokenList = s.getTokens();
        //tokenList.forEach(token -> System.out.print(token.getType()+" "));
        Parser parser = new Parser(tokenList);
        try {
            return parser.parse();
        }
        catch (Exception e){}
        return null;
    }

    private static void write(Expr e, String file){
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            ASTWriter aw = new ASTWriter(oos);
            aw.write(e);
            oos.close();
        } catch (IOException exception) {
            fatal("[Error] "+exception.getMessage());
        }
    }

    public static void main(String [] args){
        int runMode = 1; // 1 stdin, 2 file
        String runFile = null, saveFile = null;
        boolean printInfix = false, printPrefix = false, printPostfix = false;
        if(args.length > 0) {
            int precision = mathContext.getPrecision();
            RoundingMode mode = mathContext.getRoundingMode();
            List<String> as = new ArrayList<>();
            Collections.addAll(as, args);
            Object tp = getArg("precision", as.iterator(), false, false);
            if(tp !=null && tp instanceof Integer) {
                if((int) tp <= 0)
                    System.err.println("[Warning] Bad precision argument! Using default");
                else
                    precision = (int) tp;
            }
            Object rMode = getArg("rounding_mode", as.iterator(), true, false);
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
            Object file = getArg("load", as.iterator(), true, false);
            if(file != null) {
                runMode = 2;
                runFile = file.toString();
            }
            Object sfile = getArg("save", as.iterator(), true, false);
            if(sfile != null){
                if(runMode == 2){
                    System.err.println("[Warning] Using `--load`! Ignoring `--save`!");
                }
                else
                    saveFile = sfile.toString();
            }
            Object printType = getArg("display", as.iterator(), true, false);
            if(printType != null){
                String types = printType.toString();
                String [] t = types.split(",");
                for(int i = 0;i<t.length;i++){
                    if(t[i].equals("prefix"))
                        printPrefix = true;
                    else if(t[i].equals("postfix"))
                        printPostfix = true;
                    else if(t[i].equals("infix"))
                        printInfix = true;
                    else
                        System.err.println("[Warning] Bad `--display` value '"+t[i]+"' !");
                }
            }

            if(precision != mathContext.getPrecision() || mode != mathContext.getRoundingMode()) {
                System.out.println("[Info] Using precision " + precision + " and mode " +
                        mode.toString().replace("_", " ").toLowerCase()
                        + "!");
                mathContext = new MathContext(precision, mode);
            }
        }
        try {
            Solver solver = new Solver();
            Number result;
            Expr e;

            if(runMode == 1)
                e = read();
            else
                e = load(runFile);

            if(printPrefix)
                System.out.println("[Display] Prefix form : "+new ASTPrePrinter().toPrefix(e));
            if(printInfix)
                System.out.println("[Display] Infix form : "+new ASTInPrinter().toInFix(e));
            if(printPostfix)
                System.out.println("[Display] Postfix form : "+new ASTPostPrinter().toPostFix(e));

            result = solver.solve(e);

            System.out.println("[Output] Result : "+result.toString());

            if(saveFile != null){
                System.out.println("[Info] Saving to file..");
                write(e, saveFile);
                System.out.println("[Info] Saved successfully!");
            }
        }
        catch (ArithmeticException ar){
            System.err.println("[Error] "+ar.getMessage());
        }
        catch (Exception ignored){
            ignored.printStackTrace();
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

    public static void fatal(String message){
        System.err.println("[Error] "+message);
        throw new RuntimeException();
    }
}
