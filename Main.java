import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec; 

public class Main {
    private static String help = "";
    private static boolean ready = false;
    private static void init()
    {
        // Initialization of all necessary vars
        try
        {
            // Help information
            // --------------------
            File file = new File("./.help");
            FileReader reader = new FileReader(file); int data;
            while ((data = reader.read()) != -1) {Main.help += (char) data;}
            reader.close();
            // --------------------
            // Config information
            // --------------------
            file = new File("./.config");
            reader = new FileReader(file);
            while ((data = reader.read()) != -1) {}
            reader.close();
            // --------------------
            // After initialiation without errors
            // --------------------
            Main.ready = true;
            // --------------------
        }
        catch (Exception e) {/* LOG ERROR WITH E VAR*/}
    }
    private static void help()
    {
        System.out.println(Main.help);
    }
    public static void main(String[] args)
    {
        System.out.println("Encryptor is starting...");
        Main.init(); Main.help();
        if (!Main.ready) {System.out.println("Check configuration file before using the app!");}
        else
        {
            try
            {
                // Secret Key
                // --------------------------------------------------
                KeyGenerator generator = KeyGenerator.getInstance("AES");
                generator.init(256);
                SecretKey key = generator.generateKey();
                // --------------------------------------------------
                // IV - Initialization Vector
                // --------------------------------------------------
                byte[] iv = new byte[16];
                new SecureRandom().nextBytes(iv);
                IvParameterSpec param = new IvParameterSpec(iv);
                // --------------------------------------------------
                String message = "";
                // Encryption Process
                // --------------------------------------------------
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, key, param);
                // Reading from file
                // --------------------------------------------------
                File input = new File("./.input"); File output = new File("./.output");
                FileReader reader = new FileReader(input);
                FileWriter writer = new FileWriter(output);
                int data; int counter = 0;
                while ((data = reader.read()) != -1)
                {
                    message += (char) data;
                    counter += 1;
                    if (counter == 128)
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
                // --------------------------------------------------
                // At this moment serialization >>
                // And at this moment deserialization >> 
                // Decryption Process
                reader = new FileReader("./.output"); String decrypted = "";
                cipher.init(Cipher.DECRYPT_MODE, key, param);
                while ((data = reader.read()) != -1)
                {
                    if ((char) data == '\n')
                    {
                        decrypted += new String(cipher.doFinal(Base64.getDecoder().decode(message)));
                        message = "";
                    }
                    else {message += (char) data;}
                }
                // --------------------------------------------------
                reader.close(); System.out.println(decrypted);
                // --------------------------------------------------
            }
            catch (Exception e)
            {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
