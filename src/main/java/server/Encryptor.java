package server;
import java.io.FileReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import common.CryptoGen;
import common.DataGateway;
import common.Logger;
import common.RequestCode;
import common.Validator;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class Encryptor
{
    /* private boolean isRunning; */
    private WebSocketServer server;

    private Encryptor() {/* this.isRunning = true; */}

    private static boolean login(String user, String pass)
    {
        pass = CryptoGen.hash(pass);
        if (DataGateway.checkUser(user, pass)) {return true;}
        else {return false;}
    }
    private static String register(String user, String pass)
    {
        pass = CryptoGen.hash(pass);
        if (!DataGateway.checkUser(user, pass))
        {
            KeyPair keys = CryptoGen.genRSA();
            PrivateKey secretKey = CryptoGen.getPrivate(keys);
            PublicKey sharedKey = CryptoGen.getPublic(keys);
            if (DataGateway.registerUser(user, pass, CryptoGen.Public2String(sharedKey))) {return CryptoGen.Private2String(secretKey);}
            else {return null;}
        }
        else {return null;}
    }
    private static boolean encrypt(String user, String path)
    {
        try
        {
            String data = DataGateway.getRSA(user);
            PublicKey publicKey = CryptoGen.String2Public(data);
            SecretKey secretKey = CryptoGen.genAES();
            IvParameterSpec iv = CryptoGen.genIV();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int messageID = DataGateway.createMessage(user);
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
            reader.close(); return true;
        }
        catch (Exception e) {Logger.error(e.getMessage()); return false;}
    }
    private static String decrypt(String user, String path)
    {
        try
        {
            String message = "";
            FileReader reader = new FileReader(path);
            String data = "";
            int code;
            while ((code = reader.read()) != -1) {data += (char) code;}
            PrivateKey privateKey = CryptoGen.String2Private(data);
            reader.close();
            data = "";
            int mid = DataGateway.getMessageID(user);
            String encrypted = DataGateway.getMessage(mid);
            StringReader sreader;
            if (encrypted != null) {sreader = new StringReader(encrypted);}
            else {return RequestCode.NoMessages.getCode();}
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
                        message += new String(decrypted);
                        data = "";
                    }
                }
                else
                {
                    data += (char) code;
                }
            }
            Logger.info(message);
            DataGateway.deleteMessage(mid);
            sreader.close(); return message;
        }
        catch (Exception e) {e.printStackTrace(); return null;}
    }
    public static void main(String[] args)
    {
        Encryptor main = new Encryptor(); DataGateway.init();
        if (DataGateway.isReady()) {main.server = main.new Server(new InetSocketAddress("localhost", 8080));}
        else {Logger.error("PostgreSQL init error"); return;}
        Logger.info("Welcome to Encryptor Server " + Validator.checkVersion());
        main.server.run();
    }

    class Server extends WebSocketServer
    {
        public Server(InetSocketAddress address) {super(address);}

        @Override
        public void onStart() {Logger.info("Server started successfully");}
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {conn.send(RequestCode.Connected.getCode());}
        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {Logger.info("Closed connection to " + conn.getRemoteSocketAddress());}
        @Override
        public void onError(WebSocket conn, Exception ex) {}
        @Override
        public void onMessage(WebSocket conn, String message)
        {
            String[] data = message.split(" ");
            if (data[0].equals(RequestCode.Login.getCode()))
            {
                String user = Validator.checkNull(data[1]);
                String pass = Validator.checkNull(data[2]);
                if (Encryptor.login(user, pass)) {conn.send(RequestCode.Success.getCode());}
                else {conn.send(RequestCode.Failure.getCode());}
            }
            else if (data[0].equals(RequestCode.Register.getCode()))
            {
                String user = Validator.checkNull(data[1]);
                String pass = Validator.checkNull(data[2]);
                String privateKey = Encryptor.register(user, pass);
                if (privateKey != null) {conn.send(RequestCode.Success.getCode() + " " + RequestCode.Register.getCode() + " " + privateKey);}
                else {conn.send(RequestCode.Failure.getCode());}
            }
            else if (data[0].equals(RequestCode.Encrypt.getCode()))
            {
                String user = Validator.checkNull(data[1]);
                String path = Validator.checkPath(data[2]);
                if (Encryptor.encrypt(user, path)) {conn.send(RequestCode.Success.getCode());}
                else {conn.send(RequestCode.Failure.getCode());}
            }
            else if (data[0].equals(RequestCode.Decrypt.getCode()))
            {
                String user = Validator.checkNull(data[1]);
                String path = Validator.checkPath(data[2]);
                Logger.info(user); Logger.info(path);
                message = Encryptor.decrypt(user, path);
                if (message != null)
                {
                    if (message.equals(RequestCode.NoMessages.getCode())) {conn.send(RequestCode.NoMessages.getCode());}
                    else {conn.send(RequestCode.Success.getCode() + " " + RequestCode.Decrypt.getCode() + " " + message);}
                }
                else {conn.send(RequestCode.Failure.getCode());}
            }
        }
    }
}
