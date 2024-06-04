package common;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoGen
{
    public static SecretKey genAES()
    {
        try
        {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage());
            return null;
        }
    }
    public static String AES2String(SecretKey key)
    {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public static SecretKey String2AES(String key)
    {
        byte[] bytes = Base64.getDecoder().decode(key);
        return new SecretKeySpec(bytes, "AES");
    }
    public static IvParameterSpec genIV()
    {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
    public static String IV2String(IvParameterSpec iv)
    {
        return Base64.getEncoder().encodeToString(iv.getIV());
    }
    public static IvParameterSpec String2IV(String iv)
    {
        byte[] bytes = Base64.getDecoder().decode(iv);
        return new IvParameterSpec(bytes);
    }
    public static KeyPair genRSA()
    {
        try
        {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(4096);
            return generator.generateKeyPair();
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage());
            return null;
        }
    }
    public static PrivateKey getPrivate(KeyPair keys)
    {
        return keys.getPrivate();
    }
    public static String Private2String(PrivateKey key)
    {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public static PrivateKey String2Private(String key)
    {
        try
        {
            byte[] bytes = Base64.getDecoder().decode(key);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            return factory.generatePrivate(spec);
        }
        catch (Exception e)
        {
            Logger.info(e.getMessage()); return null;
        }
    }
    public static PublicKey getPublic(KeyPair keys)
    {
        return keys.getPublic();
    }
    public static String Public2String(PublicKey key)
    {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    public static PublicKey String2Public(String key)
    {
        try
        {
            byte[] bytes = Base64.getDecoder().decode(key);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            return factory.generatePublic(spec);
        }
        catch (Exception e)
        {
            Logger.info(e.getMessage()); return null;
        }
    }
    public static String hash(String data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.getBytes());
            byte[] bytes = md.digest();
            return Base64.getEncoder().encodeToString(bytes);
        }
        catch (Exception e)
        {
            Logger.error(e.getMessage()); return null;
        }
    }
}