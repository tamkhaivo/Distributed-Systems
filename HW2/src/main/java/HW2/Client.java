package HW2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/*
    Tam Vo
    CSC 258 - Distributed Systems
    HW2 - Client-Server Application

    Client.java Class that creates a socket connection to the server 
    and sends a message to the server.

    Design Requirements:
    1. Client number generation
    2. Send message with Client number
    3. Receive and display message from server
    4. Connection closes automatically via try-with-resources
    5. Must handle common errors
    
*/
public class Client {
    public static void main(String[] args) {
        int port = 8080;
        String hostname = "127.0.0.1";
        String message = "Hello, Server!";
        final int numberOfClients = 100;

        for (int clientNumber = 1; clientNumber <= numberOfClients; clientNumber++) {
            sendClientNumber(clientNumber, hostname, port, message);
        }
    }

    public static void sendClientNumber(int clientNumber, String hostname, int port, String message) {
        try (Socket socket = new Socket(hostname, port)) {
            processServerCommunication(socket, clientNumber, message);
        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        }
    }

    public static void processServerCommunication(Socket socket, int clientNumber, String message) throws IOException {
        // Setup IO streams
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Requirement #2: Send message with Client number
        String clientMessage = "Client #" + clientNumber + ": " + message;
        output.println(clientMessage);
        System.out.println("Sent: " + clientMessage);

        // Requirement #3: Receive and display
        String response = input.readLine();
        System.out.println("Received from Server: " + response);
    }
}