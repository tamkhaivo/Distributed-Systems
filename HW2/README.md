# HW2 - Client-Server Application

A simple Java-based Client-Server application demonstrating socket communication to fulfill the CSC 258 - Distributed Systems homework. The application runs on `localhost` port `8080`.

## Directory Structure
The application code is located in the `src/main/java/HW2` package. There are two primary components:
1. `Server.java` - Listens on port `8080` for incoming client connections, stamps received messages with a timestamp, and responds.
2. `Client.java` - Connects to the server, sends 100 numbered messages, and displays the server's responses.

## Prerequisites
- Java Development Kit (JDK) 17 or higher installed.
- Apache Maven installed.
- **Docker & Docker Compose** (Optional, if running the Server via Docker).

## Compilation & Testing

Open your terminal, navigate to the `HW2` directory (where the `pom.xml` is located), and run:

```bash
# Clean, compile, and run tests
mvn clean test
```

## Execution

You will need two separate terminal instances to see the interaction between the Server and the Client.

### Step 1: Start the Server

You can start the server natively using Maven, or using Docker Compose.

**Option A: Using Docker Compose**
In the first terminal window, make sure you are in the `HW2` directory and start only the server container:
```bash
docker compose up server
```

**Option B: Using Maven**
Alternatively, run the server natively down to the command line:
```bash
mvn compile exec:java -Dexec.mainClass="HW2.Server"
```

You should see an output indicating the server has started on port 8080. The server will now hang and wait to accept incoming connections.

### Step 2: Start the Client
Open a second terminal window, navigate to the `HW2` directory, and start the client. The client connects to `localhost:8080` natively (which easily connects to the Docker mapped port if you used Option A).

**Option A: Using Docker Compose to Scale Clients**
If you used Docker Compose for the server, you can dynamically spin up multiple client containers linking directly to it. To run 10 simultaneous clients:
```bash
docker compose up --scale client=10 client
```
This will automatically launch the specified number of isolated client containers, generating traffic to the server.

**Option B: Using Maven**
Alternatively, if you are running the server natively via Maven (or running mapped via Docker), you can run a single looped client via command line:
```bash
mvn compile exec:java -Dexec.mainClass="HW2.Client"
```

In either option, each Client process will establish a connection to the server and send **1** request. You will see connection indicators in the `Server` terminal, and the exchanged messages in both the `Server` and `Client` terminals. (Scaling the Docker compose will spawn 10 concurrent Client process containers simultaneously).
