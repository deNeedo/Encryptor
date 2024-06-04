package common;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;

public class Validator
{
    private static String[] commands = new String[] {"exit", "help", "encrypt", "decrypt", "login", "logout", "register"};
    public static String checkCommand(String data)
    {
        if (data == null) {return null;}
        for (String command : commands) {if (command.equals(data)) {return command;};}
        return "invalid";
    }
    public static String checkNull(String data)
    {
        if (data == null) {return null;}
        else {return data;}
    }
    public static String checkPath(String path)
    {
        try
        {
            File file = new File(path);
            FileReader reader = new FileReader(file);
            reader.close(); return path;
        }
        catch (Exception e) {Logger.error("Error while reading the file"); return null;}
    }
    public static String checkVersion() 
    {
        try
        {
            InputStream inputStream = Validator.class.getResourceAsStream("/META-INF/maven/deneedo/encryptor/pom.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            return ("v" + properties.getProperty("version"));
        }
        catch (Exception e) {return null;}
    }
}
