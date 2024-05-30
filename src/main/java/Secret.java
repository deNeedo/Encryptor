import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Secret implements Serializable
{
    public static SecretKey genKeyAES()
    {
        try
        {
            KeyGenerator generator = KeyGenerator.getInstance("AES"); generator.init(256);
            return generator.generateKey();
        }
        catch (Exception e) {return null;}
    }

    public static IvParameterSpec genIvAES()
    {
        byte[] iv = new byte[16]; new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
    public static String toString(SecretKey key)
    {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public static String toString(IvParameterSpec iv)
    {
        return Base64.getEncoder().encodeToString(iv.getIV());
    }
    // public Secret()
    // {
    //     try
    //     {
            
    //         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); String message = "test_message";
    //         cipher.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
    //         String enc = Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));

    //         Logger.info("MESSAGE ENCRYPTED :" + enc);


    //         byte[] keyBytes = Base64.getDecoder().decode(key_dump);
    //         byte[] ivBytes = Base64.getDecoder().decode(iv_dump);

    //         SecretKey key_r = new SecretKeySpec(keyBytes, "AES");
    //         IvParameterSpec iv_r = new IvParameterSpec(ivBytes);

            
    //         cipher.init(Cipher.DECRYPT_MODE, key_r, iv_r);
    //         String dec = new String(cipher.doFinal(Base64.getDecoder().decode(enc)));

    //         Logger.info("MESSAGE ENCRYPTED :" + dec);

    //         // Create a KeyPairGenerator instance for RSA
    //         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    //         keyPairGenerator.initialize(2048); // Specify the key size (2048 bits is common)
    //         // Generate the key pair
    //         KeyPair keyPair = keyPairGenerator.generateKeyPair();
    //         // Retrieve the public and private keys
    //         PublicKey publicKey = keyPair.getPublic();
    //         PrivateKey privateKey = keyPair.getPrivate();
    //         // Print out the keys in base64 encoding
    //         /*System.out.println("Public Key: " + publicKey.getEncoded());
    //         System.out.println("Private Key: " + privateKey.getEncoded());
    //         String publicS = java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded());
    //         String privateS = java.util.Base64.getEncoder().encodeToString(privateKey.getEncoded());
    //         System.out.println("Public Key: " + publicS);
    //         System.out.println("Private Key: " + privateS);
    //         System.out.println("Public Key: " + java.util.Base64.getDecoder().decode(publicS));
    //         System.out.println("Private Key: " + java.util.Base64.getDecoder().decode(privateS));*/
    //     }
    //     catch (Exception e) {Logger.error("Key generation has failed.");}
    // }
    // public void setKey(SecretKey key) {this.key = key;}
    // public void setIv(IvParameterSpec iv) {this.iv = iv;}
    // private void writeObject(ObjectOutputStream writer) throws IOException
    // {
    //     try
    //     {
    //         writer.defaultWriteObject();
    //         byte[] iv = this.iv.getIV();
    //         writer.writeInt(iv.length);
    //         writer.write(iv);
    //     }
    //     catch (Exception e) {}
    // }

    // private void readObject(ObjectInputStream reader) throws IOException, ClassNotFoundException
    // {
    //     try
    //     {
    //         reader.defaultReadObject();
    //         int ivLength = reader.readInt();
    //         byte[] iv = new byte[ivLength];
    //         reader.readFully(iv);
    //         this.iv = new IvParameterSpec(iv);
    //     }
    //     catch (Exception e) {}
    // }

    // public void serialize(String filePath)
    // {
    //     try (FileOutputStream file = new FileOutputStream(filePath); ObjectOutputStream writer = new ObjectOutputStream(file)) {writer.writeObject(this);}
    //     catch (IOException e) {Logger.error("Serialization failed.");}
    // }

    // public Secret deserialize(String filePath)
    // {
    //     try (FileInputStream file = new FileInputStream(filePath); ObjectInputStream reader = new ObjectInputStream(file)) {return (Secret) reader.readObject();}
    //     catch (Exception e) {return null;}
    // }
}