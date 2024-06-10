package client;

import common.Logger;
import common.RequestCode;
import common.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URI;
import java.util.Properties;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class Encryptor
{
    private static boolean isRunning;
    private static String requestStatus;
    private static String payload;
    private String currentUser;
    private Client client;
    private boolean initialized;
    private String address;
    private int port;

    private Encryptor()
    {
        this.initialized = false;
        Encryptor.isRunning = true;
        Encryptor.requestStatus = RequestCode.Pending.getCode();
        this.init();
    }

    private void init()
    {
        if (!this.initialized)
        {
            Properties prop = new Properties();
            String path = "./config/client.config";
            try (FileInputStream fis = new FileInputStream(path)) {prop.load(fis);}
            catch (Exception e)
            {
                Logger.warning("Config file not found at default path.");
                path = Logger.input("New path for config file: ");
                try (FileInputStream fis = new FileInputStream(path)) {prop.load(fis);}
                catch (Exception x) {return;}
            }
            this.address = prop.getProperty("client.address");
            this.port = Integer.parseInt(prop.getProperty("client.port"));
            this.initialized = true;         
        }
        else {Logger.warning("Already initialized");}
    }

    public static void main(String[] args)
    {
        Encryptor main = new Encryptor();
        if (main.initialized)
        {
            try {main.client = main.new Client(new URI("ws://" + main.address + ":" + main.port + ""));}
            catch (Exception e) {Logger.error("Cannot connect to such server"); return;}
            Logger.info("Welcome to Encryptor Client " + Validator.checkVersion());
            main.client.connect();
            synchronized (main.client)
            {
                while (!main.client.isOpen() || !main.client.isConnected() || !Encryptor.isRunning)
                {
                    try {main.client.wait();} 
                    catch (Exception e) {Logger.error("Cannot connect to such server"); return;}
                }
            }
            while (Encryptor.isRunning)
            {
                String command = Logger.input("Enter a valid command: ");
                command = Validator.checkCommand(command);
                if (command == null) {System.out.print("\n"); Logger.error("App killed"); Encryptor.isRunning = false;}
                else if (command.equals("exit"))
                {
                    main.client.close();
                    synchronized (main.client)
                    {
                        while (!main.client.isClosed())
                        {
                            try {main.client.wait();}
                            catch (Exception e) {Logger.error(e.getMessage());}
                        }
                    }
                    Logger.info("Closing app");
                    Encryptor.isRunning = false;
                }
                else if (command.equals("invalid")) {Logger.error("No such command");}
                else if (command.equals("login"))
                {
                    if (main.currentUser == null)
                    {
                        String user = Logger.input("Enter a username: ");
                        user = Validator.checkNull(user);
                        String pass = Logger.input("Enter your password: ");
                        pass = Validator.checkNull(pass);
                        main.client.setBusy(true);
                        main.client.send(RequestCode.Login.getCode() + " " + user + " " + pass);
                        synchronized (main.client)
                        {
                            while (main.client.isBusy())
                            {
                                try {main.client.wait();}
                                catch (Exception e) {Logger.error(e.getMessage());}
                            }
                        }
                        if (Encryptor.requestStatus.equals(RequestCode.Success.getCode()))
                        {
                            main.currentUser = user;
                            Logger.info("Successfully logged in");
                        }
                        else {Logger.error("Wrong username or password");}
                        Encryptor.requestStatus = RequestCode.Pending.getCode();
                    }
                    else {Logger.warning("Already logged in");}
                }
                else if (command.equals("logout"))
                {
                    if (main.currentUser != null)
                    {
                        main.currentUser = null;
                        Logger.info("Successfully logged out");
                        Encryptor.requestStatus = RequestCode.Pending.getCode();
                    }
                    else {Logger.warning("Already logged out");}
                }
                else if (command.equals("register"))
                {
                    if (main.currentUser == null)
                    {
                        String user = Logger.input("Enter a username: ");
                        user = Validator.checkNull(user);
                        String pass = Logger.input("Enter your password: ");
                        pass = Validator.checkNull(pass);
                        main.client.setBusy(true);
                        main.client.send(RequestCode.Register.getCode() + " " + user + " " + pass);
                        synchronized (main.client)
                        {
                            while (main.client.isBusy())
                            {
                                try {main.client.wait();}
                                catch (Exception e) {Logger.error(e.getMessage());}
                            }
                        }
                        if (Encryptor.requestStatus.equals(RequestCode.Success.getCode()))
                        {
                            try
                            {
                                File file = new File("./secret/" + user + ".key"); File dir = new File("./secret");
                                if (!dir.exists()) {dir.mkdirs();}
                                FileWriter writer = new FileWriter(file);
                                writer.write(Encryptor.payload); writer.close();
                            }
                            catch (Exception e) {Logger.error(e.getMessage());}
                            Logger.info("Successfully registered");
                        }
                        else {Logger.error("Such user already exists in the system");}
                        Encryptor.requestStatus = RequestCode.Pending.getCode();
                        Encryptor.payload = null;
                    }
                    else {Logger.warning("Already logged in");}
                }
                else if (command.equals("encrypt"))
                {
                    if (main.currentUser != null)
                    {
                        String path = Logger.input("Enter a path for the file you wish to encrypt: ");
                        path = Validator.checkPath(path);
                        if (path == null) {Logger.error("Error while reading the file"); continue;}
                        String user = Logger.input("Enter a username of your recipient: ");
                        user = Validator.checkNull(user);
                        main.client.setBusy(true);
                        main.client.send(RequestCode.Encrypt.getCode() + " " + user + " " + path);
                        synchronized (main.client)
                        {
                            while (main.client.isBusy())
                            {
                                try {main.client.wait();}
                                catch (Exception e) {Logger.error(e.getMessage());}
                            }
                        }
                        if (Encryptor.requestStatus.equals(RequestCode.Success.getCode()))
                        {
                            Logger.info("Successfully encrypted the data");
                        }
                        else {Logger.error("Unexpected error while encrypting the data");}
                        Encryptor.requestStatus = RequestCode.Pending.getCode();
                    }
                    else {Logger.error("Not authorized");}
                }
                else if (command.equals("decrypt"))
                {
                    if (main.currentUser != null)
                    {
                        String path = Logger.input("Enter a path for the secret key: ");
                        path = Validator.checkPath(path);
                        if (path == null) {Logger.error("Error while reading the file"); continue;}
                        main.client.setBusy(true);
                        main.client.send(RequestCode.Decrypt.getCode() + " " + main.currentUser + " " + path);
                        synchronized (main.client)
                        {
                            while (main.client.isBusy())
                            {
                                try {main.client.wait();}
                                catch (Exception e) {Logger.error(e.getMessage());}
                            }
                        }
                        Logger.info(Encryptor.requestStatus);
                        if (Encryptor.requestStatus.equals(RequestCode.Success.getCode()))
                        {
                            Logger.info("Successfully decrypted the data");
                            System.out.println(Encryptor.payload);
                        }
                        else if (Encryptor.requestStatus.equals(RequestCode.NoMessages.getCode()))
                        {
                            Logger.warning("No messages to decrypt");
                        }
                        else {Logger.error("Unexpected error while decrypting the data");}
                        Encryptor.requestStatus = RequestCode.Pending.getCode();
                        Encryptor.payload = null;
                    }
                    else {Logger.error("Not authorized");}
                }
            }
            return;
        }
        else {Logger.error("Client init error"); return;}
    }

    class Client extends WebSocketClient
    {
        private boolean isBusy = false;
        private boolean isConnected = false;

        public Client(URI serverUri) {super(serverUri);}

        public boolean isBusy() {return this.isBusy;}
        public void setBusy(boolean status) {this.isBusy = status;}
        public boolean isConnected() {return this.isConnected;}
        @Override
        public void onOpen(ServerHandshake handshakedata)
        {
            Logger.info("Connection opened");
            synchronized (this) {notify();}
        }
        @Override
        public void onClose(int code, String reason, boolean remote)
        {
            Logger.info("Connection closed");
            synchronized (this) {notify();}
        }
        @Override
        public void onError(Exception e)
        {
            Logger.error("Connection error");
            Encryptor.isRunning = false;
            synchronized (this) {notify();}
        }
        @Override
        public void onMessage(String message)
        {
            if (message.equals(RequestCode.Connected.getCode()))
            {
                Logger.info("Successfully connected to the server");
                this.isConnected = true;
                synchronized (this) {notify();}
            }
            else if (message.contains(RequestCode.Success.getCode()))
            {
                if (message.contains(RequestCode.Register.getCode()))
                {
                    Encryptor.payload = message.split(" ")[2];
                }
                else if (message.contains(RequestCode.Decrypt.getCode()))
                {
                    int counter = 0; Encryptor.payload = "";
                    for (String message_part : message.split(" "))
                    {
                        if (counter < 2) {counter += 1;}
                        else {Encryptor.payload += (message_part + " ");}
                    }
                }
                Encryptor.requestStatus = RequestCode.Success.getCode();
                this.isBusy = false;
                synchronized (this) {notify();}
            }
            else if (message.contains(RequestCode.Failure.getCode()))
            {
                Encryptor.requestStatus = RequestCode.Failure.getCode();
                this.isBusy = false;
                synchronized (this) {notify();}
            }
            if (message.equals(RequestCode.NoMessages.getCode()))
            {
                Encryptor.requestStatus = RequestCode.NoMessages.getCode();
                this.isBusy = false;
                synchronized (this) {notify();}
            }
        }
    }
}