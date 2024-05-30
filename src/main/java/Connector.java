import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class Connector
{
    private static Properties props;
    private static String address;
    private static String port;
    private static String database;
    private static String url;
    private static Connection conn;
    private static boolean initialized = false;

    public static boolean getStatus() {return Connector.initialized;}

    public static void init()
    {
        if (!Connector.initialized)
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
            Connector.props = new Properties();
            Connector.props.setProperty("user", prop.getProperty("db.user"));
            Connector.props.setProperty("password", prop.getProperty("db.password"));

            Connector.address = prop.getProperty("db.address");
            Connector.port = prop.getProperty("db.port");
            Connector.database = prop.getProperty("db.database");
            Connector.url = "jdbc:postgresql://" + Connector.address + ":" + Connector.port + "/" + Connector.database;
            Connector.initialized = true;         
        }
        else {Logger.warning("Already initialized");}
    }
    public static String getRSA(String user)
    {
        try
        {
            Connector.conn = DriverManager.getConnection(Connector.url, Connector.props);
            PreparedStatement ps = conn.prepareStatement("SELECT key FROM users WHERE username = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            String key = null;
            while (rs.next()) {key = rs.getString("key");}
            ps.close(); conn.close();
            return key;
        }
        catch (Exception e) {Logger.error("Cannot connect"); e.printStackTrace(); return null;}
    }

    public static void registerUser(String user, String pass, String publicKey)
    {
        try
        {
            Connector.conn = DriverManager.getConnection(Connector.url, Connector.props);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password, key) VALUES (?, ?, ?)");
            ps.setString(1, user); ps.setString(2, pass) ;ps.setString(3, publicKey);
            ps.executeUpdate(); ps.close(); conn.close();
        }
        catch (Exception e) {Logger.error("Cannot connect"); e.printStackTrace();}
    }
    public static boolean checkUser(String user) 
    {
        try
        {
            Connector.conn = DriverManager.getConnection(Connector.url, Connector.props);
            PreparedStatement ps = conn.prepareStatement("SELECT username FROM users WHERE username = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery(); String result = null;
            while (rs.next()) {result = rs.getString("name");}
            ps.close(); conn.close();
            if (result == null) {return true;}
        }
        catch (Exception e) {}
        return false;
        
    }
}
