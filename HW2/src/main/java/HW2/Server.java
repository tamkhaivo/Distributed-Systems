package HW2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
    Tam Vo
    CSC 258 - Distributed Systems
    HW2 - Client-Server Application

    Server.java Class that creates a socket connection to the client 
    and sends a message to the client.
    
    Design Requirements:
    1. Establish Socket connection to client
    2. Receive data from the client with client number and a message
    3. Send back original message with timestamp to the client.
    4. Connection closes automatically
    5. Must handle more than one client
    6. Must handle common errors 

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

    public ClientHandler(Socket socket) {
        this._socket = socket;
    }

    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
                PrintWriter output = new PrintWriter(_socket.getOutputStream(), true)) {
            String clientMessage = input.readLine();
            if (clientMessage != null) {
                // Requirement #1: Process message: Add timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String response = "[Server Received at " + timestamp + "] " + clientMessage;

                // Wait for 1 second
                try {
                    Thread.sleep(1000);
                    // Requirement #2: Send back to client
                    output.println(response);
                } catch (InterruptedException e) {
                    System.err.println("Server Error: " + e.getMessage());
                }
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
}
