import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Base64;
import javax.crypto.Cipher;

public class Encryptor
{
    private final String algorithm = "AES/CBC/PKCS5Padding";
    private boolean generate = false;
    private boolean ready = false;
    private int buffer = 16;
    private Secret secret;
    private String output;
    private String input;
    private String mode;
    
    private Encryptor() {}

    private boolean init(String[] args)
    {
        try
        {
            Logger.info("Encryptor is starting...");
            for (int m = 0; m < args.length; m++)
            {
                if (args[m].contains("-buffer="))
                {
                    Logger.info("Changing buffer size...");
                    buffer = Integer.parseInt(args[m].split("=")[1]);
                }
                else if (args[m].equals("-gennewkey")) {generate = true;}
                else if (args[m].contains("-input=")) {this.input = args[m].split("=")[1];}
                else if (args[m].contains("-output=")) {this.output = args[m].split("=")[1];}
                else if (args[m].equals("-enc")) {this.mode = "encrypt";}
                else if (args[m].equals("-dec")) {this.mode = "decrypt";}
            }
            Logger.info("The new key is being generated...");
            this.secret = new Secret();
            if (this.generate) {secret.serialize("./.secret");}
            /* check if .serial exists */
            else {this.secret = secret.deserialize("./.secret");}
            Logger.info("Checking I/O paths...");
            File file = new File(this.input); FileReader reader = new FileReader(file); reader.close();
            file = new File(this.output); FileWriter writer = new FileWriter(file); writer.close();
            Logger.info("Selecting operation mode...");
            if (this.mode == null) {int m = 1 / 0; System.out.println(m);}
            return true;
        }
        catch (Exception e)
        {
            Logger.error("App failed to initialize. Make sure to follow the instructions listed below."); this.help();
            return false;
        }
    }
    private void help()
    {
        Logger.info("Encryptor uses call arguments to determine neccessary information for the encryption process.");
        Logger.info("Below you can see the list of Encryptor's parameters along with some example calls.");
        Logger.info("Arguments that can be set by the user: \n -gennewkey | tells Encryptor to generate key secret instead of using the old one. \n Note that after selecting this option any already encrypted files are gonna be lost as the information about the old secret gets discarded in the process of creating the new secret.");
    }

    // private void serialize(Secret secret)
    // {
    //     try
    //     {   
    //         FileOutputStream file = new FileOutputStream("./.secret");
    //         ObjectOutputStream writer = new ObjectOutputStream(file);
    //         writer.writeObject(secret); writer.close(); file.close();
    //     }
    //     // catch (Exception e) {e.printStackTrace();}
    //     catch (Exception e) {Logger.error("Unexpected error during serialization process");}
    // }

    // private Secret deserialize()
    // {
    //     try
    //     {
    //         System.out.println();
    //         FileInputStream file = new FileInputStream("./.secret");
    //         ObjectInputStream reader = new ObjectInputStream(file);
    //         Secret secret = (Secret) reader.readObject(); reader.close(); file.close();
    //         return secret;
    //     }
    //     catch (Exception e) {Logger.error("Unexpected error during deserialization process"); return null;}
        
    // }

    private void encrypt()
    {
        try
        {
            Cipher cipher = Cipher.getInstance(this.algorithm); String message = "";
            FileReader reader = new FileReader(this.input); FileWriter writer = new FileWriter(this.output);
            cipher.init(Cipher.ENCRYPT_MODE, secret.getKey(), secret.getIv()); int data; int counter = 0;
            while ((data = reader.read()) != -1)
            {
                message += (char) data;
                counter += 1;
                if (counter == this.buffer)
                {
                    byte[] cipher_str = cipher.doFinal(message.getBytes());
                    writer.write(Base64.getEncoder().encodeToString(cipher_str) + "\n");
                    counter = 0; message = "";
                }
            }
            if (message.length() != 0)
            {
                byte[] cipher_str = cipher.doFinal(message.getBytes());
                writer.write(Base64.getEncoder().encodeToString(cipher_str) + "\n");
                counter = 0; message = "";
            }
            reader.close(); writer.close();
        }
        catch (Exception e) {Logger.error("Unexpected error during encryption process");}
    }

    private void decrypt()
    {
        try
        {
            Cipher cipher = Cipher.getInstance(this.algorithm); String message = "";
            FileReader reader = new FileReader(this.input); FileWriter writer = new FileWriter(this.output);
            cipher.init(Cipher.DECRYPT_MODE, secret.getKey(), secret.getIv()); int data;
            while ((data = reader.read()) != -1)
            {
                if ((char) data == '\n')
                {
                    writer.write(new String(cipher.doFinal(Base64.getDecoder().decode(message))));
                    message = "";
                }
                else {message += (char) data;}
            }
            reader.close(); writer.close();

        }
        catch (Exception e) {Logger.error("Unexpected error during decryption process");}
    }

    public static void main(String[] args)
    {
        Encryptor main = new Encryptor();
        main.ready = main.init(args);
        try
        {
            if (main.ready)
            {
                if (main.mode.equals("encrypt")) {main.encrypt();}
                else if (main.mode.equals("decrypt")) {main.decrypt();}
            }
        }
        catch (Exception e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
