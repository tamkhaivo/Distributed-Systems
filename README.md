# Distributed Systems HW1 - Client-Server Application

This project implements a multi-threaded Client-Server application in Java, demonstrating socket programming, concurrency, and basic distributed systems concepts.

## Features
-   **Server**: Handles multiple concurrent clients using threads. Timestamps incoming messages and echoes them back.
-   **Client**: Connects to the server, sends a message with a client ID, and prints the server's response.
-   **Testing**: Includes a comprehensive JUnit 5 test suite with Mockito to simulate network conditions.

## Prerequisites
-   **Java JDK 17+**
-   **Maven** (`mvn`)

## Project Commands

### 1. Run Tests
Execute the JUnit 5 test suite (including Client-Server integration tests with Mockito):
```bash
mvn test
```

### 2. Compile Code
To compile the source code without running tests:
```bash
mvn compile
```

### 3. Clean Build Artifacts
Remove the `target/` directory to clean previous builds:
```bash
mvn clean
```

### 4. Package JAR
Create a distributable JAR file (in `target/`):
```bash
mvn package
```

### 5. Run Server (Manual)
To run the Server application manually:
```bash
mvn exec:java -Dexec.mainClass="HW1.Server"
```

### 6. Run Client (Manual)
To run the Client application manually (requires Server to be running):
```bash
mvn exec:java -Dexec.mainClass="HW1.Client"
```
