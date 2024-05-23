/* Necessary imports */
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Base64;
import javax.crypto.Cipher;
/* Main class */
public class Encryptor
{
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
    /* Empty constructor (needed to avoid 'serial' keyword) */
    private Encryptor() {}
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
                    File file = new File(this.output); FileWriter writer = new FileWriter(file); writer.close();
                }
                catch (Exception e) {Logger.error("Error while setting path for output file. Most likely provided value contains non-accessible path in your system."); return false;}
            }
            /* -------------------- */
        }
        if (this.input == null) {Logger.error("Missing input file."); return false;}
        if (this.output == null) {Logger.error("Missing output file."); return false;}
        if ((this.mode == 0) && (this.bufferSet)) {Logger.warning("Buffer is not being used in DECRYPT mode. No need to change its value.");}
        if ((this.secretPath == null) && (!this.generated)) {Logger.error("Missing argument with the path for storing/accessing the secret."); return false;}
        if ((this.secretPath != null) && (!this.generated))
        {
            try
            {
                File file = new File(this.secretPath); FileReader reader = new FileReader(file);
                int data = reader.read(); reader.close(); if (data == -1) {Logger.error("Provided input file is empty."); return false;}
            }
            catch (Exception e) {Logger.error("Missing file with secret."); return false;}
        }
        if (this.generated) {Logger.info("Serializing secret object"); this.secret.serialize(this.secretPath);}
        if (!this.generated)
        {
            try {Logger.info("Deserializing secret object"); this.secret = new Secret(); this.secret = this.secret.deserialize(this.secretPath);}
            catch (Exception e) {Logger.error("Deserialization error. Most likely the file does not contain secret."); return false;}
        }
        return true;
    }
    private void help() {}
    private void encrypt()
    {
        try
        {
            Cipher cipher = Cipher.getInstance(this.algorithm); String message = "";
            FileReader reader = new FileReader(this.input); FileWriter writer = new FileWriter(this.output);
            cipher.init(Cipher.ENCRYPT_MODE, this.secret.getKey(), this.secret.getIv()); int data; int counter = 0;
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
            cipher.init(Cipher.DECRYPT_MODE, this.secret.getKey(), this.secret.getIv()); int data;
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
                if (main.mode == 1) {main.encrypt();}
                else if (main.mode == 0) {main.decrypt();}
            }
        }
        catch (Exception e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
