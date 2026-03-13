package HW3.src.main.java.HW3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

/*
    Tam Vo
    CSC 258 - Distributed Systems
    HW2 - Client-Server Application

    Client.java Class that creates a socket connection to the server 
    and sends a message to the server.

    Design Requirements (HW3 Rubric):
    1. File access - client side (Implemented via processServerCommunication READ/WRITE)
    2. File access - Multiple clients (Handled via simulation script & concurrency locks)
    3. Testing and results (Client output logged, integration tests written)
*/
public class Client {
    static final int MAX_MESSAGES = 100;

    public static void main(String[] args) {
        int port = 8080;
        String hostname = System.getenv("SERVER_HOST");
        String clientIdEnv = System.getenv("CLIENT_ID");
        String message = randomMessage();
        Random random = new Random();

        if (hostname == null || hostname.isEmpty()) {
            hostname = "127.0.0.1";
        }
        if (clientIdEnv == null || clientIdEnv.isEmpty()) {
            clientIdEnv = Integer.toString(random.nextInt(1, 100));
        }

        int clientNumber = Integer.parseInt(clientIdEnv);
        sendMessages(clientNumber, hostname, port, message);
    }

    public static String randomMessage() {
        String[] messages = { "READ", "WRITE Hello, Server!" };
        Random random = new Random();
        return messages[random.nextInt(messages.length)];
    }

    public static void sendMessages(int clientNumber, String hostname, int port, String messageBase) {
        try (Socket socket = new Socket(hostname, port)) {
            processServerCommunication(socket, clientNumber, messageBase, true);
        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        }
    }

    public static Set<String> processServerCommunication(Socket socket, int clientNumber, String messageBase,
            boolean simulateDelay) throws IOException {
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Set<String> messageReceived = new HashSet<>();

        for (int messageCount = 1; messageCount <= MAX_MESSAGES; messageCount++) {
            if (simulateDelay) {
                sleep(TimeUnit.SECONDS, 1);
            }

            // HW3 Rubric: File access - client side
            String messageBaseLoop = randomMessage();
            String clientMessage;
            if (messageBaseLoop.equals("READ")) {
                clientMessage = "READ"; // Client requests to read the file
            } else {
                clientMessage = messageBaseLoop + " from Client #" + clientNumber; // Client modifies the file
            }
            output.println(clientMessage);
            System.out.println("Client #" + clientNumber + " sent: " + clientMessage);

            // Requirement #3: Receive and display
            String response = input.readLine();
            if (response == null) {
                System.out.println("Server disconnected");
                break;
            }
            messageReceived.add(response);
        }
        System.out.println("Client " + clientNumber + " received " + messageReceived.size() + " messages:");
        for (String message : messageReceived) {
            System.out.println(message);
        }
        return messageReceived;
    }

    public static void sleep(TimeUnit unit, int ms) {
        try {
            Thread.sleep(unit.toMillis(ms));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
