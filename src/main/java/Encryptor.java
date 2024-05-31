import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
/* Main class */
public class Encryptor
{
    private boolean isAuthorized;
    private boolean isRunning;
    private Encryptor()
    {
        this.isAuthorized = false;
        this.isRunning = true;
        DataGateway.init();
    }
    private void register(String username, String password)
    {
        try
        {
            KeyPair keys = CryptoGen.genRSA();
            PrivateKey secretKey = CryptoGen.getPrivate(keys);
            PublicKey sharedKey = CryptoGen.getPublic(keys);
            String hash = CryptoGen.hash(password);
            DataGateway.registerUser(username, hash, CryptoGen.Public2String(sharedKey));
            /* change to server client transmission */
            /* ------------------------------------ */
            File dir = new File("./secret");
            if (!dir.exists()) dir.mkdirs();
            File file = new File("./secret/secret.key");
            FileWriter writer = new FileWriter(file);
            writer.write(CryptoGen.Private2String(secretKey));
            writer.close();
            /* ------------------------------------ */
            Logger.info("User successfully created");
        }
        catch (Exception e) {Logger.error(e.getMessage());}
    }
    private void encrypt(String recipient, String path)
    {
        try
        {
            String data = DataGateway.getRSA(recipient);
            PublicKey publicKey = CryptoGen.String2Public(data);

            SecretKey secretKey = CryptoGen.genAES();
            IvParameterSpec iv = CryptoGen.genIV();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            /* need a separate class for file handling */
            File dir = new File("./temp");
            if (!dir.exists()) dir.mkdirs();
            FileWriter writer = new FileWriter("./temp/encrypted");
            data = CryptoGen.AES2String(secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            writer.write(Base64.getEncoder().encodeToString(encrypted) + "\n");
            data = CryptoGen.IV2String(iv);
            encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            writer.write(Base64.getEncoder().encodeToString(encrypted) + "\n");

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            FileReader reader = new FileReader(path);
            String temp = "";
            int code;
            while ((code = reader.read()) != -1)
            {
                temp += (char) code;
                if (temp.length() == 16)
                {
                    encrypted = cipher.doFinal(temp.getBytes(StandardCharsets.UTF_8));
                    writer.write(Base64.getEncoder().encodeToString(encrypted) + "\n");
                    temp = "";
                }
            }
            if (temp.length() != 0)
            {
                encrypted = cipher.doFinal(temp.getBytes(StandardCharsets.UTF_8)); 
                writer.write(Base64.getEncoder().encodeToString(encrypted) + "\n");
                temp = "";
            }
            reader.close();
            writer.close();
        }
        catch (Exception e) {e.printStackTrace();}
    }

    private void decrypt(String path)
    {
        try
        {
            File file = new File("./secret/secret.key");
            File dir = new File("./secret");
            if (!dir.exists()) dir.mkdirs();
            FileReader reader = new FileReader(file);
            String data = "";
            int code;
            while ((code = reader.read()) != -1) {data += (char) code;}
            PrivateKey privateKey = CryptoGen.String2Private(data);
            reader.close();
            data = "";

            file = new File("./temp/encrypted");
            dir = new File("./temp");
            if (!dir.exists()) dir.mkdirs();
            reader = new FileReader(file);
            FileWriter writer = new FileWriter("./temp/decrypted");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            SecretKey secret = null; IvParameterSpec iv = null; int flag = 1;
            while ((code = reader.read()) != -1)
            {
                if ((char) code == '\n')
                {
                    if (flag == 1)
                    {
                        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data));
                        secret = CryptoGen.String2AES(new String(decrypted));
                        flag = 0;
                        data = "";
                    }
                    else if (flag == 0)
                    {
                        Logger.info("IV after reading from file: " + data);
                        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data));
                        iv = CryptoGen.String2IV(new String(decrypted));
                        flag = -1;
                        data = "";
                        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.DECRYPT_MODE, secret, iv);
                    }
                    else
                    {
                        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data));
                        writer.write(new String(decrypted));
                        data = "";
                    }
                }
                else
                {
                    data += (char) code;
                }
            }
            reader.close();
            writer.close();
        }
        catch (Exception e) {Logger.error("Unexpected error during decryption process"); e.printStackTrace();}
    }

    public static void main(String[] args) throws Exception
    {
        Encryptor main = new Encryptor();
        // main.register("test", "test");
        // main.encrypt("test", "./config/db.config");
        // main.decrypt("./temp/stage1");
        
        if (DataGateway.isReady())
        {
            while (main.isRunning)
            {
                String input = Logger.input("Enter a valid command: ");
                String code = Validator.checkCommand(input);
                if (code == null) {System.out.print("\n"); Logger.error("000: App killed"); main.isRunning = false;}
                else if (code == "exit") {Logger.info("000: App closing"); main.isRunning = false;}
                // else if (code == "help") {Logger.info("000: App usage");}
                else if (code == "invalid") {Logger.error("000: No such command");}
                else if (code == "encrypt" || code == "enc")
                {
                    // if (main.authorized) {Logger.info("000: Encrypting"); main.encrypt();}
                    // else {Logger.error("000: Not authorized");}
                    String path = Logger.input("Enter a path for the file you wish to encrypt: ");
                    String key = null;
                    if (Validator.checkPath(path))
                    {
                        String user = Logger.input("Enter a username of your recipient: ");
                        key = DataGateway.getRSA(user);
                    }
                    if (key != null) {Logger.info("000: Encrypting"); main.encrypt(path, key);}
                    else {Logger.error("000: No such user in the system");}
                }
                else if (code == "decrypt" || code == "dec") {
                    // if (main.authorized) {Logger.info("000: Decrypting"); main.decrypt();}
                    // else {Logger.error("000: Not authorized");}
                    String path = Logger.input("Enter a path for storing the decrypted message: ");
                    if (Validator.checkPath(path))
                    {
                        main.decrypt(path);
                    }
                }
                else if (code == "register" || code == "reg")
                {
                    if (!main.isAuthorized)
                    {
                        String user = Logger.input("Enter a username: ");
                        String pass = null;
                        if (DataGateway.fetchUser(user))
                        {
                            pass = Logger.input("Enter a passphrase: ");
                            if (Validator.checkPass(pass))
                            {
                                Logger.info("000: Signing up"); main.register(user, pass);
                            }
                        }
                        else {Logger.error("Provided username is already taken");}
                    }
                    else
                    {
                        Logger.warning("000: Already logged in");
                    }
                }
                // else if (code == "deregister" || code == "dereg")
                // {
                //     if (main.authorized) {Logger.info("000: Unsubscribing"); main.deregister();}
                //     else {Logger.error("000: Not authorized");}
                // }
                // else if (code == "login")
                // {
                //     if (!main.authorized) {Logger.info("000: Singing in"); main.login();}
                //     Logger.warning("000: Already logged in");
                //     Logger.input("Log in to different account? (y/n): ");
                // }
                // else if (code == "logout")
                // {
                //     if (main.authorized) {Logger.info("000: Signing out"); main.logout();}
                //     else {Logger.error("000: Not authorized");}
                // }
            }
        }
    }
}
