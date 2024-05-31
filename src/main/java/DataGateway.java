import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class DataGateway
{
    private static Properties props;
    private static String address;
    private static String port;
    private static String database;
    private static String url;
    private static boolean initialized = false;

    public static boolean isReady() {return DataGateway.initialized;}

    public static void init()
    {
        if (!DataGateway.initialized)
        {
            Properties prop = new Properties();
            String path = "./config/db.config";
            try (FileInputStream fis = new FileInputStream(path)) {prop.load(fis);}
            catch (Exception e)
            {
                Logger.warning("Config file not found at default path.");
                path = Logger.input("New path for config file: ");
                try (FileInputStream fis = new FileInputStream(path)) {prop.load(fis);}
                catch (Exception x) {return;}
            }
            DataGateway.props = new Properties();
            DataGateway.props.setProperty("user", prop.getProperty("db.user"));
            DataGateway.props.setProperty("password", prop.getProperty("db.password"));

            DataGateway.address = prop.getProperty("db.address");
            DataGateway.port = prop.getProperty("db.port");
            DataGateway.database = prop.getProperty("db.database");
            DataGateway.url = "jdbc:postgresql://" + DataGateway.address + ":" + DataGateway.port + "/" + DataGateway.database;
            DataGateway.initialized = true;         
        }
        else {Logger.warning("Already initialized");}
    }
    public static String getRSA(String user)
    {
        try
        {
            Connection conn = DriverManager.getConnection(DataGateway.url, DataGateway.props);
            PreparedStatement ps = conn.prepareStatement("SELECT key FROM users WHERE name = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            String key = null;
            while (rs.next()) {key = rs.getString("key");}
            ps.close(); conn.close();
            return key;
        }
        catch (Exception e) {Logger.error("Cannot connect"); return null;}
    }

    public static void registerUser(String username, String password, String publicKey)
    {
        try
        {
            Connection conn = DriverManager.getConnection(DataGateway.url, DataGateway.props);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO users (name, password, key) VALUES (?, ?, ?)");
            ps.setString(1, username); ps.setString(2, password); ps.setString(3, publicKey);
            int result = ps.executeUpdate();
            ps.close(); conn.close();
            if (result == 0) {throw new Exception("Unexpected error while adding new user");}
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage());
        }
    }
    public static boolean fetchUser(String user)
    {
        try
        {
            Connection conn = DriverManager.getConnection(DataGateway.url, DataGateway.props);
            PreparedStatement ps = conn.prepareStatement("SELECT name FROM users WHERE name = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery(); String result = null;
            while (rs.next()) {result = rs.getString("name");}
            ps.close(); conn.close();
            if (result == null) {return true;}
        }
        catch (Exception e) {}
        return false;
    }
    public static String getMessage(String user)
    {
        try
        {
            Connection conn = DriverManager.getConnection(DataGateway.url, DataGateway.props);
            PreparedStatement ps = conn.prepareStatement("SELECT message FROM messages WHERE name = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery(); String result = null;
            while (rs.next()) {result = rs.getString("message");}
            ps.close(); conn.close();
            if (result != null) {return result;}
        }
        catch (Exception e) {}
        return null;
    }
}
