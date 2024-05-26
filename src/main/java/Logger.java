public class Logger
{
    private static String reset = "\u001B[0m";
    // private static String black = "\u001B[30m";
    private static String red = "\u001B[31m";
    private static String green = "\u001B[32m";
    private static String yellow = "\u001B[33m";
    // private static String blue = "\u001B[34m";
    // private static String purple = "\u001B[35m";
    // private static String cyan = "\u001B[36m";
    // private static String white = "\u001B[37m";
    public static void info(String text) {System.out.println("[" + Logger.green + "INFO" + Logger.reset + "] " + text);}
    public static void warning(String text) {System.out.println("[" + Logger.yellow + "WARNING" + Logger.reset + "] " + text);}
    public static void error(String text) {System.out.println("[" + Logger.red + "ERROR" + Logger.reset + "] " + text);}
}
