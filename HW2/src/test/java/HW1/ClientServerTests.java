package HW1;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.net.Socket;

public class ClientServerTests {

    @Mock
    private Socket mockSocket;

    @Mock
    private OutputStream mockOutputStream;

    @Mock
    private InputStream mockInputStream;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(mockSocket.getInputStream()).thenReturn(mockInputStream);
    }

    @Test
    public void testClientProcessCommunication() throws IOException {
        // Set up mock socket to return expected message
        String expectedMessage = "Hello from Server";
        ByteArrayInputStream serverResponse = new ByteArrayInputStream((expectedMessage + "\n").getBytes());
        when(mockSocket.getInputStream()).thenReturn(serverResponse);

        ByteArrayOutputStream clientOutput = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(clientOutput);

        // Act
        Client.processServerCommunication(mockSocket, 1, "Test Message");

        // Assert
        // Verify Client sent the correct message
        String output = clientOutput.toString();
        assertTrue(output.contains("Client #1: Test Message"));

        // Verify Socket streams were accessed
        verify(mockSocket).getOutputStream();
        verify(mockSocket).getInputStream();
    }

    @Test
    public void testSocketIOException() throws IOException {
        // Set up mock socket to throw exception on output stream access
        when(mockSocket.getOutputStream()).thenThrow(new IOException("Simulated Connection Failure"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            Client.processServerCommunication(mockSocket, 1, "Test Message");
        });
    }

    // Test ClientHandler I/O Error
    @Test
    public void testClientHandlerIOException() throws IOException {
        // Set up mock socket to throw exception on input stream access
        when(mockSocket.getInputStream()).thenThrow(new IOException("Simulated I/O Failure"));

        // Capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Act
            ClientHandler handler = new ClientHandler(mockSocket);
            handler.run();

            // Assert
            String errOutput = errContent.toString();
            assertTrue(errOutput.contains("I/O Error handling client: Simulated I/O Failure"));
        } finally {
            System.setErr(originalErr);
        }
    }

    // Test ClientHandler Generic Exception
    @Test
    public void testClientHandlerGenericException() throws IOException {
        // Set up mock socket to throw exception on input stream access
        when(mockSocket.getInputStream()).thenThrow(new RuntimeException("Simulated Generic Failure"));

        // Capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Act
            ClientHandler handler = new ClientHandler(mockSocket);
            handler.run();

            // Assert
            String errOutput = errContent.toString();
            assertTrue(errOutput.contains("Generic Error handling client: Simulated Generic Failure"));
        } finally {
            System.setErr(originalErr);
        }
    }

    // Test ClientHandler Connection Close I/O Error
    @Test
    public void testClientHandlerCloseIOException() throws IOException {
        // Set up mock socket to throw exception on close()
        ByteArrayInputStream input = new ByteArrayInputStream("Test\n".getBytes());
        when(mockSocket.getInputStream()).thenReturn(input);
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        // Mock exception on close()
        doThrow(new IOException("Simulated Close Failure")).when(mockSocket).close();

        // Capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Act
            ClientHandler handler = new ClientHandler(mockSocket);
            handler.run();

            // Assert
            String errOutput = errContent.toString();
            assertTrue(errOutput.contains("I/O Error closing socket: Simulated Close Failure"));
        } finally {
            System.setErr(originalErr);
        }
    }

    // Test ClientHandler Connection Close Generic Exception
    @Test
    public void testClientHandlerCloseGenericException() throws IOException {
        // Set up mock socket to throw exception on close()
        ByteArrayInputStream input = new ByteArrayInputStream("Test\n".getBytes());
        when(mockSocket.getInputStream()).thenReturn(input);
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        // Mock exception on close()
        doThrow(new RuntimeException("Simulated Close Generic Failure")).when(mockSocket).close();

        // Capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            // Act
            ClientHandler handler = new ClientHandler(mockSocket);
            handler.run();

            // Assert
            String errOutput = errContent.toString();
            assertTrue(errOutput.contains("Generic Error closing socket: Simulated Close Generic Failure"));
        } finally {
            System.setErr(originalErr);
        }
    }
}
