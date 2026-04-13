package HW3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
    Tam Vo
    CSC 258 - Distributed Systems
    HW2 - Client-Server Application

    Server.java Class that creates a socket connection to the client 
    and sends a message to the client.
    
    Design Requirements (HW3 Rubric):
    1. File access - server side (Implemented via readLogFile/appendToFile and rwLock)
    2. File access - Multiple clients (Handled via ReentrantReadWriteLock for safe concurrent access)
    3. Testing and results (Server creates/updates server.log transparently)
*/
public class Server {

    public static void main(String[] args) {
        int port = 8080;
        try (ServerSocket helloServer = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            for (;;) {
                Socket socket = helloServer.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }
}

/*
 * ClientHandler
 * Requirement #5: Extends Thread to handle multiple clients
 * Requirement #6: Must handle common errors when processing client requests
 */
class ClientHandler extends Thread {
    private Socket _socket;
    private static final String LOG_FILE = "server.log";
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public ClientHandler(Socket socket) {
        this._socket = socket;
    }

    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
                PrintWriter output = new PrintWriter(_socket.getOutputStream(), true)) {
            String clientMessage;
            while ((clientMessage = input.readLine()) != null) {
                String response = processClientMessage(clientMessage);
                output.println(response);
            }
        } catch (IOException e) {
            System.err.println("I/O Error handling client: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Generic Error handling client: " + e.getMessage());
        } finally {
            try {
                // Requirement #4: Connection closes automatically
                _socket.close();
            } catch (IOException e) {
                System.err.println("I/O Error closing socket: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Generic Error closing socket: " + e.getMessage());
            }
        }
    }

    private static String processClientMessage(String clientMessage) {
        String[] parts = clientMessage.split(" ", 2);
        if (parts.length < 1) {
            return "Invalid message format";
        }

        String command = parts[0];

        if (command.equals("READ")) {
            return readLogFile();
        } else if (command.equals("WRITE") && parts.length == 2) {
            String message = parts[1];
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            return appendToFile("[" + timestamp + "] " + message);
        }
        return "Unknown command";
    }

    private static void initalizeLog(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("I/O Error creating log file: " + e.getMessage());
        }
    }

    // HW3 Rubric: READ File access
    private static String readLogFile() {
        rwLock.readLock().lock(); // HW3 Rubric: File access - Multiple clients (Thread-safe reads)
        try {
            File file = new File(LOG_FILE);
            initalizeLog(file);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder logContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                logContent.append(line).append(" | ");
            }
            br.close();
            fr.close();
            String result = logContent.toString();
            return result.isEmpty() ? "[Empty Log]" : result;
        } catch (IOException e) {
            System.err.println("I/O Error reading log file: " + e.getMessage());
            return "[ERROR]: " + e.getMessage();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // HW3 Rubric: WRITE File access
    private static String appendToFile(String message) {
        rwLock.writeLock().lock(); // HW3 Rubric: File access - Multiple clients (Thread-safe exclusive writes)
        try {
            File file = new File(LOG_FILE);
            initalizeLog(file);
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(message);
            bw.newLine();
            bw.close();
            return "[LOGGED]: " + message;
        } catch (IOException e) {
            System.err.println("I/O Error appending to file: " + e.getMessage());
            return "[ERROR]: " + e.getMessage();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
