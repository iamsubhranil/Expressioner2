package com.iamsubhranil.expressioner2;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class Expressioner {

    private static String source = "";
    private static boolean debug = false;
    static MathContext mathContext = MathContext.DECIMAL128;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

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
                        warning("Incomplete "+param.replace("_"," ")+
                                " argument! Using default "+param.replace("_"," ")+"!");
                        return null;
                    }
                }
                try{
                    return Integer.valueOf(pre);
                }
                catch (NumberFormatException nfe){
                    if(!allowString)
                        warning("Bad "+param+" argument! Using default "+param+"!");
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
            ASTReader ar = new ASTReader(ois);
            Expr e = ar.read();
            ois.close();
            fis.close();
            return e;
        } catch (IOException e) {
            fatal(e.getMessage());
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
            fatal(exception.getMessage());
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
                    warning("Bad precision argument! Using default");
                else
                    precision = (int) tp;
            }
            Object rMode = getArg("rounding_mode", as.iterator(), true, false);
            if(rMode != null && rMode instanceof String){
                try{
                    mode = RoundingMode.valueOf((String)rMode);
                }
                catch (IllegalArgumentException iae){
                    warning("Bad rounding mode argument! Using default!");
                }
            }
            else if(rMode != null){
                try {
                    mode = RoundingMode.valueOf((int)rMode);
                }
                catch (IllegalArgumentException iae){
                    warning("Bad rounding mode argument! Using default!");
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
                    warning("Using `--load`! Ignoring `--save`!");
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
                        warning("Bad `--display` value '"+t[i]+"' !");
                }
            }
            Object dbg = getArg("verbose", as.iterator(), true, true);
            if(dbg != null)
                debug = true;

            if(precision != mathContext.getPrecision() || mode != mathContext.getRoundingMode()) {
                info("Using precision " + precision + " and mode " +
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

            if(saveFile != null){
                info("Saving to file..");
                write(e, saveFile);
                info("Saved successfully!");
            }

            result = solver.solve(e);

            System.out.println("[Output] Result : "+result.toString());
        }
        catch (ArithmeticException ar){
            System.err.println(ANSI_RED+"[Error] "+ar.getMessage()+ANSI_RESET);
        }
        catch (Exception ignored){
           // ignored.printStackTrace();
        }
    }

    public static void error(int position, String message){
        System.err.println(ANSI_RED+"[Error:"+position+"] "+source+ANSI_RESET);
        System.err.print(ANSI_RED+"[Error:"+position+"] "+ANSI_RED);
        for(int i = 0;i < position;i++)
            System.err.print(" ");
        System.err.print(ANSI_RED+"^\n"+ANSI_RESET);
        System.err.println(ANSI_RED+"[Error:"+position+"] "+message+ANSI_RESET);
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
        System.err.println(ANSI_RED+"[Error] "+message+ANSI_RESET);
        throw new RuntimeException();
    }

    public static void warning(String message){
        System.out.println(ANSI_YELLOW+"[Warning] "+message+ANSI_RESET);
    }

    public static void info(String message){
        System.out.println(ANSI_BLUE+"[Info] "+message+ANSI_RESET);
    }

    public static void debug(String message){
        if(debug)
            System.out.println(ANSI_GREEN+"[Debug] "+message+ANSI_RESET);
    }
}
