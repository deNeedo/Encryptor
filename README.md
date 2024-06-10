# Encryptor
## Overview
Encryptor is a secure and reliable application designed to safely store confidential information in an encrypted format. With a robust client-server architecture, Encryptor ensures that your sensitive data is protected both during transit and at rest.
## Features
- **User Authentication**: Register and log in to the system securely to access your encrypted messages.
- **Multi-User Support**: Send and receive messages to and from any other user registered on the platform.
- **Secure Storage**: Encrypt your confidential messages and store them safely on the server.
- **Advanced Encryption**: Cutting-edge encryption to ensure your data is secure.
## Getting Started
### Prerequisites
#### Common (Server & Client)
- **Java**: Java(TM) SE Runtime Environment (build 21.0.3+7-LTS-152), Java(TM) SE Runtime Environment (build 1.8.0_411-b09)
#### Server
- **PostgreSQL**: PostgreSQL 16.1

Remark: Versions of the software mentioned above were used during development and testing process, however other versions might still support it.
### Installation
#### Configuration files
- **Provide config for database**:
```bash
[Default config path: ./config/db.config]
[Contents of the file should be as below:]
db.address=[POSTGRES OPERATING IP ADDRESS]
db.port=[POSTGRES OPERATING PORT]
db.database=encryptor
db.user=[POSTGRES USER]
db.password=[POSTGRES PASSWORD]
[Replace the [...] with your personal configuration.]
```
- **Provide config for server app**:
```bash
[Default config path: ./config/server.config]
[Contents of the file should be as below:]
server.address=[SERVER OPERATING IP ADDRESS]
server.port=[SERVER OPERATING PORT]
[Replace the [...] with your personal configuration.]
```
- **Provide config for client app**:
```bash
[Default config path: ./config/client.config]
[Contents of the file should be as below:]
client.address=[CLIENT OPERATING IP ADDRESS]
client.port=[CLIENT OPERATING PORT]
[Replace the [...] with your personal configuration.]
```
#### Quickstart approach
- **Download the assets from**: https://github.com/deNeedo/Encryptor/releases/latest

Namely you are interested in these files: **server-X.Y.jar**, **client-X.Y.jar** and **setup.sql** where **X** adn **Y** represents the version indicator.
- **Initialize database with setup script**:
```bash
psql -f ./path/to/setup.sql
```
#### Manual compilation
- **Clone the repository**
```bash
git clone https://github.com/deNeedo/Encryptor.git
```
- **Compile the project using Maven** (for development and testing process Apache Maven 3.9.6 was used)
```bash
mvn clean
mvn install -P server
mvn install -P client
```
- **Initialize database with setup script**:
```bash
psql -f ./database/setup.sql
```
### Running the Application
- **Start the Server**
```bash
[X adn Y represents the version indicator]
java -jar ./target/server-X.Y.jar
```
- **Start the Client**
```bash
[X adn Y represents the version indicator]
java -jar ./target/client-X.Y.jar
```
### Usage
#### Server
After running the app server should initialize connection to the database and you should see something like that:

![server_start](./assets/server_start.png)
#### Client
After running the app client should initialize connection to the server and you should see something like that:

![client_start](./assets/client_start.png)

**Supported commands are**: register, login, logout, encrypt, decrypt, exit

##### Register command example
The correct command execution process looks like this:

![client_register_success](./assets/client_register_success.png)

When you try to register using the username that already exists in the system, you will see this log:

![client_register_failure](./assets/client_register_failure.png)

##### Login command example
The correct command execution process looks like this:

![client_login_success](./assets/client_login_success.png)

If you are already logged in to the system, you will see this log:

![client_login_warning](./assets/client_login_warning.png)

When you try to log in, using the username that does not exists in the system, you will see this log:

![client_login_failure](./assets/client_login_failure.png)

##### Logout command example
The correct command execution process looks like this:

![client_logout_success](./assets/client_logout_success.png)

When you try to log out, while not being logged in prior to that, you will see this log:

![client_logout_warning](./assets/client_logout_warning.png)

##### Exit command example
The correct command execution process looks like this:

![client_exit_gentle](./assets/client_exit_gentle.png)

However you can still close the app using keyboard interrupts (for instance Ctrl + C):

![client_exit_forced](./assets/client_exit_forced.png)

##### Encrypt command example
The correct command execution process looks like this:

![client_encrypt_success](./assets/client_encrypt_success.png)

If you try to use this command, while not being logged in, you will see this log:

![client_encrypt_not_authorized](./assets/client_encrypt_not_authorized.png)

If you provide a non existing path to the message you wish to encrypt, you will see this log:

![client_encrypt_no_such_path](./assets/client_encrypt_no_such_path.png)

If you make a mistake while providing user to whom you would like to send this message, you will see this log:

![client_encrypt_no_such_user](./assets/client_encrypt_no_such_user.png)

##### Decrypt command example
The correct command execution process looks like this:

![client_decrypt_success](./assets/client_decrypt_success.png)

However, if you have no encrypted messages waiting for you, you will see this log:

![client_decrypt_no_messages](./assets/client_decrypt_no_messages.png)

If you try to use this command, while not being logged in, you will see this log:

![client_decrypt_not_authorized](./assets/client_decrypt_not_authorized.png)

If you provide a non existing path to the message you wish to encrypt, you will see this log:

![client_decrypt_no_such_path](./assets/client_decrypt_no_such_path.png)
