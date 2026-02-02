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
        // Arrange
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
        // Arrange
        when(mockSocket.getOutputStream()).thenThrow(new IOException("Simulated Connection Failure"));

        // Act & Assert
        // Client.processServerCommunication allows IOException to bubble up to be
        // caught by caller
        assertThrows(IOException.class, () -> {
            Client.processServerCommunication(mockSocket, 1, "Test Message");
        });
    }
}
