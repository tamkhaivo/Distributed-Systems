# HW2 - Client-Server Application

A simple Java-based Client-Server application demonstrating socket communication to fulfill the CSC 258 - Distributed Systems homework. The application runs on `localhost` port `8080`.

## Directory Structure
The application code is located in the `src/main/java/HW2` package. There are two primary components:
1. `Server.java` - Listens on port `8080` for incoming client connections, stamps received messages with a timestamp, and responds.
2. `Client.java` - Connects to the server, sends 100 numbered messages, and displays the server's responses.

## Prerequisites
- Java Development Kit (JDK) 17 or higher installed.
- Apache Maven installed.

## Compilation & Testing

Open your terminal, navigate to the `HW2` directory (where the `pom.xml` is located), and run:

```bash
# Clean, compile, and run tests
mvn clean test
```

## Execution

You will need two separate terminal instances to see the interaction between the Server and the Client.

### Step 1: Start the Server
In the first terminal window, make sure you are in the `HW2` directory and start the server:

```bash
mvn compile exec:java -Dexec.mainClass="HW2.Server"
```
You should see output similar to: `Server started on port 8080`. The server will now hang and wait to accept incoming connections.

### Step 2: Start the Client
Open a second terminal window, navigate to the `HW2` directory, and start the client:

```bash
mvn compile exec:java -Dexec.mainClass="HW2.Client"
```

The client will connect to the server and send 100 requests. You will see connection indicators in the `Server` terminal, and the exchanged messages in both the `Server` and `Client` terminals.
