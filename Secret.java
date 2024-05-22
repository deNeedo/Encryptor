import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Secret implements Serializable
{
    private SecretKey key;
    private transient IvParameterSpec iv;
    public Secret()
    {
        try
        {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            this.key = generator.generateKey();
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            this.iv = new IvParameterSpec(iv);
        }
        catch (Exception e) {Logger.error("Key generation has failed.");}
    }
    public SecretKey getKey() {return this.key;}
    public IvParameterSpec getIv() {return this.iv;}
    public void setKey(SecretKey key) {this.key = key;}
    public void setIv(IvParameterSpec iv) {this.iv = iv;}
    private void writeObject(ObjectOutputStream writer) throws IOException
    {
        try
        {
            writer.defaultWriteObject();
            byte[] iv = this.iv.getIV();
            writer.writeInt(iv.length);
            writer.write(iv);
        }
        catch (Exception e) {}
    }

    private void readObject(ObjectInputStream reader) throws IOException, ClassNotFoundException
    {
        try
        {
            reader.defaultReadObject();
            int ivLength = reader.readInt();
            byte[] iv = new byte[ivLength];
            reader.readFully(iv);
            this.iv = new IvParameterSpec(iv);
        }
        catch (Exception e) {}
    }

    public void serialize(String filePath) {
        try (FileOutputStream file = new FileOutputStream(filePath);
             ObjectOutputStream writer = new ObjectOutputStream(file)) {
            writer.writeObject(this);
        } catch (IOException e) {Logger.error("Serialization failed.");}
    }

    public Secret deserialize(String filePath) {
        try (FileInputStream file = new FileInputStream(filePath);
             ObjectInputStream reader = new ObjectInputStream(file)) {
            return (Secret) reader.readObject();
        } catch (IOException | ClassNotFoundException e) {Logger.error("Deserialization failed."); return null;}
    }
}