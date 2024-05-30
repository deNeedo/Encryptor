import java.io.BufferedReader;
import java.io.InputStreamReader;
public class Logger
{
    private static String reset = "\u001B[0m";
    // private static String black = "\u001B[30m";
    private static String red = "\u001B[31m";
    private static String green = "\u001B[32m";
    private static String yellow = "\u001B[33m";
    private static String blue = "\u001B[34m";
    // private static String purple = "\u001B[35m";
    // private static String cyan = "\u001B[36m";
    // private static String white = "\u001B[37m";
    public static void info(String message) {System.out.println("[" + Logger.green + "INFO" + Logger.reset + "] " + message);}
    public static String input(String message)
    {
        try
        {
            System.out.print("[" + Logger.blue + "INPUT" + Logger.reset + "] " + message);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String data = reader.readLine(); return data;
        }
        catch (Exception e) {return null;}
    }
    public static void warning(String message) {System.out.println("[" + Logger.yellow + "WARNING" + Logger.reset + "] " + message);}
    public static void error(String message) {System.out.println("[" + Logger.red + "ERROR" + Logger.reset + "] " + message);}
}
