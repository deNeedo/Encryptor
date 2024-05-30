/* Necessary imports */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
/* Main class */
public class Encryptor
{
    private boolean running;
    private boolean authorized;

    /* Encryption/decryption related fields */
    private int mode = 1;
    private boolean modeSet = false;
    private int buffer = 16;
    private boolean bufferSet = false;
    private final String algorithm = "AES/CBC/PKCS5Padding";
    /* Secret related fields */
    private boolean generated = false;
    private String secretPath = null;
    private Secret secret = null;
    private boolean ready = false;
    /* I/O related fields */
    private String input = null;
    private String output = null;
    private String helpPath = "help.txt";
    /* Constructor */
    private Encryptor() {this.running = true; this.authorized = false;}
    
    
    
    private void register(String user, String pass)
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            String publicRSA = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            Connector.registerUser(user, pass, publicRSA);
        }
        catch (Exception e) {}
    }
    private void deregister() {}
    private void login() {}
    private void logout() {}
    
    

    
    /* Method for preparing class variables to work as expected and also to check correctness of user input */
    private boolean init(String[] args)
    {
        Logger.info("Encryptor is starting.");
        for (int m = 0; m < args.length; m++)
        {
            /* Encryption/decryption related settings */
            /* -------------------------------------- */
            if (args[m].equals("-enc"))
            {
                Logger.info("Setting operation mode to ENCRYPT.");
                if (this.modeSet) {Logger.error("Error while setting operation mode. Value already set."); return false;}
                else {this.mode = 1; this.modeSet = true;}
            }
            else if (args[m].contains("-buffer="))
            {
                Logger.info("Changing buffer size.");
                try {buffer = Integer.parseInt(args[m].split("=")[1]); this.bufferSet = true;}
                catch (Exception e) {Logger.error("Error while setting buffer size. Most likely provided value is NOT an integer."); return false;}
            }
            else if (args[m].equals("-dec"))
            {
                Logger.info("Setting operation mode to DECRYPT.");
                if (this.modeSet) {Logger.error("Error while setting operation mode. Value already set."); return false;}
                else {this.mode = 0; this.modeSet = true;}
            }
            /* -------------------------------------- */
            /* Secret related settings */
            /* ----------------------- */
            else if (args[m].equals("-gennew")) {Logger.info("Generating new secret."); this.secret = new Secret(); this.generated = true;}
            else if (args[m].contains("-secret="))
            {
                try {Logger.info("Setting path for secret storing."); this.secretPath = args[m].split("=")[1];}
                catch (Exception e) {Logger.error("Error while setting path for storing secret. Most likely provided value contains non-accessible path in your system."); return false;}
            }
            /* ----------------------- */
            /* I/O related settings */
            /* -------------------- */
            else if (args[m].contains("-input="))
            {
                try
                {
                    Logger.info("Setting path for the input file."); this.input = args[m].split("=")[1];
                    if (this.input.equals(this.output)) {Logger.error("Input and output cannot point to the same file."); return false;}
                    File file = new File(this.input); FileReader reader = new FileReader(file);
                    int data = reader.read(); reader.close(); if (data == -1) {Logger.error("Provided input file is empty."); return false;}
                }
                catch (Exception e) {Logger.error("Error while setting path for input file. Most likely provided value contains non-existing path in your system."); return false;}
            }
            else if (args[m].contains("-output="))
            {
                try
                {
                    Logger.info("Setting path for the output file."); this.output = args[m].split("=")[1];
                    if (this.input.equals(this.output)) {Logger.error("Input and output cannot point to the same file."); return false;}
                }
                catch (Exception e) {Logger.error("Error while setting path for output file. Most likely provided value contains non-accessible path in your system."); return false;}
            }
            /* -------------------- */
        }
        // if (this.generated && (this.mode == 0)) {Logger.error("It is only possible to decrypt the message using the same secret as was used to encrypt it."); return false;}
        // if (this.input == null) {Logger.error("Missing input file."); return false;}
        // if (this.output == null) {Logger.error("Missing output file."); return false;}
        // if ((this.mode == 0) && (this.bufferSet)) {Logger.warning("Buffer is not being used in DECRYPT mode. No need to change its value.");}
        // if (this.secretPath == null) {Logger.error("Missing argument with the path for storing/accessing the secret."); return false;}
        // if ((this.secretPath != null) && (!this.generated))
        // {
        //     try
        //     {
        //         File file = new File(this.secretPath); FileReader reader = new FileReader(file);
        //         int data = reader.read(); reader.close(); if (data == -1) {Logger.error("Provided input file is empty."); return false;}
        //     }
        //     catch (Exception e) {Logger.error("Missing file with secret."); return false;}
        // }
        // if (this.generated) {Logger.info("Serializing secret object"); this.secret.serialize(this.secretPath);}
        // if (!this.generated)
        // {
        //     try {Logger.info("Deserializing secret object"); this.secret = new Secret(); this.secret = this.secret.deserialize(this.secretPath);}
        //     catch (Exception e) {Logger.error("Deserialization error. Most likely the file does not contain secret."); return false;}
        // }
        // if (this.output != null)
        // {
        //     try {File file = new File(this.output); FileWriter writer = new FileWriter(file); writer.close();}
        //     catch (Exception e) {Logger.error("Output file reading error. Most likely path points to the file that is not accessible by the system."); return false;}
        // }
        return true;
    }
    private void help()
    {
        Logger.info("Help function call");
        // try
        // {
        //     InputStream file = Encryptor.class.getClassLoader().getResourceAsStream(this.helpPath);
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(file));
        //     int data; String message = ""; int counter = 0;
        //     while ((data = reader.read()) != -1)
        //     {
        //         message += (char) data; counter += 1;
        //         if (counter == this.buffer) {System.out.print(message); counter = 0; message = "";}
        //     }
        //     if (message.length() != 0) {System.out.print(message); counter = 0; message = "";} reader.close();
        // }
        // catch (Exception e) {Logger.error("Error while executing 'help' function. Most likely the file is missing.");}
    }
    private void encrypt(String inputPath, String keyRSA)
    {
        try
        {
            // stage 1
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); String message = "";
            SecretKey keyAES = Secret.genKeyAES(); IvParameterSpec ivAES = Secret.genIvAES();
            cipher.init(Cipher.ENCRYPT_MODE, keyAES, ivAES); int data;
            FileReader reader = new FileReader(inputPath);
            FileWriter writer = new FileWriter("./temp/stage1");
            writer.write(Secret.toString(keyAES) + "\n");
            writer.write(Secret.toString(ivAES) + "\n");
            while ((data = reader.read()) != -1)
            {
                message += (char) data;
                if (message.length() == 64)
                {
                    byte[] cipher_str = cipher.doFinal(message.getBytes()); message = "";
                    writer.write(Base64.getEncoder().encodeToString(cipher_str) + "\n");
                }
            }
            if (message.length() != 0)
            {
                byte[] cipher_str = cipher.doFinal(message.getBytes()); message = "";
                writer.write(Base64.getEncoder().encodeToString(cipher_str) + "\n");
            }
            reader.close(); writer.close();
            // stage 2
            byte[] publicKeyBytes = Base64.getDecoder().decode(keyRSA);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicRSA = keyFactory.generatePublic(keySpec);
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicRSA);
            reader = new FileReader("./temp/stage1");
            writer = new FileWriter("./temp/stage2");
            while ((data = reader.read()) != -1)
            {
                message += (char) data;
                if (message.length() == 64)
                {
                    byte[] cipher_str = cipher.doFinal(message.getBytes()); message = "";
                    writer.write(Base64.getEncoder().encodeToString(cipher_str) + "\n");
                }
            }
            if (message.length() != 0)
            {
                byte[] cipher_str = cipher.doFinal(message.getBytes()); message = "";
                writer.write(Base64.getEncoder().encodeToString(cipher_str) + "\n");
            }
            reader.close(); writer.close();
        }
        catch (Exception e) {Logger.error("Unexpected error during encryption process"); e.printStackTrace();}
    }

    private void decrypt()
    {
        // try
        // {
        //     Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); String message = "";
        //     FileReader reader = new FileReader(this.input); FileWriter writer = new FileWriter(this.output);
        //     cipher.init(Cipher.DECRYPT_MODE, this.secret.getKey(), this.secret.getIv()); int data;
        //     while ((data = reader.read()) != -1)
        //     {
        //         if ((char) data == '\n')
        //         {
        //             writer.write(new String(cipher.doFinal(Base64.getDecoder().decode(message))));
        //             message = "";
        //         }
        //         else {message += (char) data;}
        //     }
        //     reader.close(); writer.close();
        // }
        // catch (Exception e) {Logger.error("Unexpected error during decryption process");}
    }

    public static void main(String[] args)
    {
        Encryptor main = new Encryptor(); Connector.init();
        if (Connector.getStatus())
        {
            while (main.running)
            {
                String userInput = Logger.input("Enter a valid command: ");
                String code = Validator.checkCommand(userInput);
                if (code == null) {System.out.print("\n"); Logger.error("000: App killed"); main.running = false;}
                else if (code == "exit") {Logger.info("000: App closing"); main.running = false;}
                else if (code == "help") {Logger.info("000: App usage");}
                else if (code == "invalid") {Logger.error("000: No such command"); main.help();}
                else if (code == "encrypt" || code == "enc")
                {
                    // if (main.authorized) {Logger.info("000: Encrypting"); main.encrypt();}
                    // else {Logger.error("000: Not authorized");}
                    String path = Logger.input("Enter a path for the file you wish to encrypt: ");
                    String key = null;
                    if (Validator.checkPath(path))
                    {
                        String user = Logger.input("Enter a username of your recipient: ");
                        key = Connector.getRSA(user);
                    }
                    if (key != null) {Logger.info("000: Encrypting"); main.encrypt(path, key);}
                }
                // else if (code == "decrypt" || code == "dec") {
                //     if (main.authorized) {Logger.info("000: Decrypting"); main.decrypt();}
                //     else {Logger.error("000: Not authorized");}
                // }
                else if (code == "register" || code == "reg")
                {
                    if (!main.authorized)
                    {
                        String user = Logger.input("Enter a username: ");
                        String pass = null;
                        if (Connector.checkUser(user))
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
                        Logger.warning("000: Already logged in"); Logger.input("Log out? (y/n): ");
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
        
        /* -------------------------------------------------- */
        // main.ready = main.init(args);
        // try
        // {
        //     if (main.ready)
        //     {
        //         if (main.mode == 1) {boolean encrypted = main.encrypt(); if (encrypted) {Logger.info("File successfully encrypted.");}}
        //         else if (main.mode == 0) {boolean decrypted = main.decrypt(); if (decrypted) {Logger.info("File successfully decrypted.");}}
        //     }
        //     else {main.help();}
        // }
        // catch (Exception e)
        // {
        //     System.out.println("An error occurred.");
        //     e.printStackTrace();
        // }
    }
}
