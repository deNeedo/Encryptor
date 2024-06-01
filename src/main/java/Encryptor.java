import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Encryptor
{
    private String currentUser;
    private boolean isRunning;
    private Encryptor()
    {
        this.currentUser = null;
        this.isRunning = true;
        DataGateway.init();
    }
    private boolean login(String username, String password)
    {
        try
        {
            String hash = CryptoGen.hash(password);
            DataGateway.loginUser(username, hash);
            return true;
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage());
            return false;
        }
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
    private void encrypt(String path, String recipient)
    {
        try
        {
            String data = DataGateway.getRSA(recipient);
            PublicKey publicKey = CryptoGen.String2Public(data);

            SecretKey secretKey = CryptoGen.genAES();
            IvParameterSpec iv = CryptoGen.genIV();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            int messageID = DataGateway.createMessage(recipient);
            data = CryptoGen.AES2String(secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            DataGateway.updateMessage(messageID, Base64.getEncoder().encodeToString(encrypted) + "\n");
            data = CryptoGen.IV2String(iv);
            encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            DataGateway.updateMessage(messageID, Base64.getEncoder().encodeToString(encrypted) + "\n");

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
                    DataGateway.updateMessage(messageID, Base64.getEncoder().encodeToString(encrypted) + "\n");
                    temp = "";
                }
            }
            if (temp.length() != 0)
            {
                encrypted = cipher.doFinal(temp.getBytes(StandardCharsets.UTF_8)); 
                DataGateway.updateMessage(messageID, Base64.getEncoder().encodeToString(encrypted) + "\n");
                temp = "";
            }
            reader.close();
        }
        catch (Exception e) {e.printStackTrace();}
    }

    private void decrypt(String keyPath, String user)
    {
        try
        {
            FileReader reader = new FileReader(keyPath);
            String data = "";
            int code;
            while ((code = reader.read()) != -1) {data += (char) code;}
            PrivateKey privateKey = CryptoGen.String2Private(data);
            reader.close();
            data = "";
            String encrypted = DataGateway.getMessage(user);
            StringReader sreader;
            if (encrypted != null)
            {sreader = new StringReader(encrypted);}
            else {throw new Exception("No messages to decrypt");}
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            SecretKey secret = null; IvParameterSpec iv = null; int flag = 1;
            while ((code = sreader.read()) != -1)
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
                        System.out.print(new String(decrypted));
                        data = "";
                    }
                }
                else
                {
                    data += (char) code;
                }
            }
            System.out.print("\n");
            sreader.close();
        }
        catch (Exception e) {Logger.error(e.getMessage());}
    }

    public static void main(String[] args) throws Exception
    {
        Encryptor main = new Encryptor();
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
                    if (main.currentUser != null)
                    {
                        String path = Logger.input("Enter a path for the file you wish to encrypt: ");
                        String user = "";
                        if (Validator.checkPath(path))
                        {
                            user = Logger.input("Enter a username of your recipient: ");
                            if (DataGateway.fetchUser(user)) {Logger.info("000: Encrypting"); main.encrypt(path, user);}
                            else {Logger.error("000: No such user in the system");}
                        }
                    }
                    else {Logger.error("000: Not authorized");}
                }
                else if (code == "decrypt" || code == "dec")
                {
                    if (main.currentUser != null)
                    {
                        String keyPath = Logger.input("Enter a path for secret key: ");
                        if (Validator.checkPath(keyPath))
                        {
                            main.decrypt(keyPath, main.currentUser);
                        }
                    }
                    else {Logger.error("000: Not authorized");}
                }
                else if (code == "register" || code == "reg")
                {
                    if (main.currentUser == null)
                    {
                        String user = Logger.input("Enter a username: ");
                        String pass = null;
                        if (!DataGateway.fetchUser(user))
                        {
                            pass = Logger.input("Enter a passphrase: ");
                            if (Validator.checkPass(pass))
                            {
                                Logger.info("000: Signing up"); 
                                main.register(user, pass);
                                main.currentUser = user;
                            }
                        }
                        else {Logger.error("Provided username is already taken");}
                    }
                    else {Logger.warning("000: Already logged in");}
                }
                else if (code == "login")
                {
                    if (main.currentUser == null)
                    {
                        String user = Logger.input("Enter a username: ");
                        String pass = null;
                        if (DataGateway.fetchUser(user))
                        {
                            pass = Logger.input("Enter a passphrase: ");
                            if (Validator.checkPass(pass))
                            {
                                Logger.info("000: Signing in");
                                if (main.login(user, pass)) main.currentUser = user;
                            }
                        }
                        else {Logger.error("No such user in the system");}
                    }
                    else Logger.warning("000: Already logged in");
                }
                else if (code == "logout")
                {
                    if (main.currentUser != null) {Logger.info("000: Signing out"); main.currentUser = null;}
                    else {Logger.error("000: User not logged in");}
                }
            }
        }
    }
}
