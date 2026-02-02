package HW1;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
    Tam Vo
    CSC 258 - Distributed Systems
    HW1 - Client-Server Application

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
        }
    }
}

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
                // Process message: Add timestamp (Requirement #1)
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String response = "[Server Received at " + timestamp + "] " + clientMessage;

                // Send back to client (Requirement #2)
                output.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                _socket.close(); // Ensure connection is closed
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
