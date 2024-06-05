package client;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import common.Logger;
import common.RequestCode;
import common.Validator;

public class Encryptor
{
    private boolean isRunning;
    private static String requestStatus;
    private static String payload;
    private String currentUser;
    private Client client;

    private Encryptor()
    {
        this.isRunning = true;
        Encryptor.requestStatus = RequestCode.Pending.getCode();
    }

    public static void main(String[] args)
    {
        Encryptor main = new Encryptor();
        try {main.client = main.new Client(new URI("ws://localhost:8080"));}
        catch (Exception e) {Logger.error(e.getMessage());}
        Logger.info("Welcome to Encryptor Client " + Validator.checkVersion());
        main.client.connect();
        synchronized (main.client)
        {
            while (!main.client.isOpen() || !main.client.isConnected())
            {
                try {main.client.wait();} 
                catch (Exception e) {Logger.error(e.getMessage());}
            } 
        }
        while (main.isRunning)
        {
            String command = Logger.input("Enter a valid command: ");
            command = Validator.checkCommand(command);
            if (command == null) {System.out.print("\n"); Logger.error("App killed"); main.isRunning = false;}
            else if (command.equals("exit"))
            {
                main.client.close();
                synchronized (main.client)
                {
                    while (!main.client.isClosed())
                    {
                        Logger.warning("closing");
                        try {main.client.wait();}
                        catch (Exception e) {Logger.error(e.getMessage());}
                    }
                }
                Logger.info("Closing app");
                main.isRunning = false;
            }
            else if (command.equals("invalid")) {Logger.error("No such command");}
            // else if (command.equals("help")) {}
            else if (command.equals("login"))
            {
                if (main.currentUser == null)
                {
                    String user = Logger.input("Enter a username: ");
                    user = Validator.checkNull(user);
                    String pass = Logger.input("Enter your password: ");
                    pass = Validator.checkNull(pass);
                    main.client.setBusy(true);
                    main.client.send(RequestCode.Login.getCode() + ":" + user + ":" + pass);
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
                    Logger.error("Successfully logged out");
                    Encryptor.requestStatus = RequestCode.Pending.getCode();
                }
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
                    main.client.send(RequestCode.Register.getCode() + ":" + user + ":" + pass);
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
                            File file = new File("./secret/secret.key"); File dir = new File("./secret");
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
                    if (Encryptor.requestStatus.equals(RequestCode.Success.getCode()))
                    {
                        try
                        {
                            File file = new File("./secret/message.decrypted"); File dir = new File("./secret");
                            if (!dir.exists()) {dir.mkdirs();}
                            FileWriter writer = new FileWriter(file);
                            writer.write(Encryptor.payload); writer.close();
                        }
                        catch (Exception e) {Logger.error(e.getMessage());}
                        Logger.info("Successfully decrypted the data");
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
            e.printStackTrace();
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