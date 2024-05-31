import java.io.File;
import java.io.FileReader;

public class Validator {
    private static String[] commands = new String[] {"exit", "help", "encrypt", "enc", "decrypt", "dec", "login", "logout", "register", "reg", "deregister", "dereg"};
    public static String checkCommand(String data) {
        if (data == null) return null;
        for (int m = 0; m < commands.length; m++)
        {
            if (commands[m].equals(data)) return commands[m];
        }
        return "invalid";
    }
    public static String checkConfig(String configPath) {
        return "";
    }
    public static boolean checkPath(String path) {
        try
        {
            File file = new File(path);
            FileReader reader = new FileReader(file);
            reader.close(); return true;
        }
        catch (Exception e) {Logger.error("Error while reading the file"); return false;}
    }
    public static boolean checkPass(String password)
    {
        if (password != null) return true;
        else return false;
    }
}
