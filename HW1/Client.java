package HW1;

import java.io.*;
import java.net.*;

/*
    Tam Vo
    CSC 258 - Distributed Systems
    HW1 - Client-Server Application

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
        int clientNumber = 1; // Requirement #2: Client number generation

        try (Socket socket = new Socket(hostname, port)) {
            // Setup IO streams
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Requirement #2: Send message with Client number
            String clientMessage = "Client #" + clientNumber + ": " + message;
            output.println(clientMessage);
            System.out.println("Sent: " + clientMessage);

            // Requirement #2: Receive and display
            String response = input.readLine();
            System.out.println("Received from Server: " + response);

            // Requirement #2: Connection closes automatically via try-with-resources
        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        }
    }
}

/*
 * Bash Code used to test multiple clients
 * 
 * for (( i = 1; i <= 100; i++ )); do
 * java Client
 * done
 * 
 */
