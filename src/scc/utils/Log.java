package scc.utils;

public class Log {
    static boolean DEBUG = true;
    public static void d(String tag, String s){
        if (DEBUG)
            System.out.println("DEBUG/"+tag+": "+s);
    }
    public static void i(String tag,String s){
        if (DEBUG)
            System.out.println("INFO/"+tag+": "+s);
    }
}
