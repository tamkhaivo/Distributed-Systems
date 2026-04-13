package HW3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientTest {

    @Mock
    private Socket mockSocket;

    @Test
    public void testProcessServerCommunication_ReceivesMessages() throws IOException {
        // Arrange
        // We will simulate the server answering 5 times
        String simulatedServerResponses = "Response 1\nResponse 2\nResponse 3\nResponse 4\nResponse 5\n";
        InputStream mockInputStream = new ByteArrayInputStream(simulatedServerResponses.getBytes());
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(mockInputStream);
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);

        int clientNumber = 1;
        String messageBase = "TEST";
        boolean simulateDelay = false;

        // Act
        Set<String> result = Client.processServerCommunication(mockSocket, clientNumber, messageBase, simulateDelay);

        // Assert
        assertEquals(5, result.size(), "Should have populated the set with exactly 5 unique responses");
        assertTrue(result.contains("Response 1"), "Set should contain 'Response 1'");
        assertTrue(result.contains("Response 5"), "Set should contain 'Response 5'");

        // Also verify the output stream has written 6 messages to the server (5 before disconnect + 1 that discovers the disconnect)
        String outputContent = mockOutputStream.toString();
        String[] sentMessages = outputContent.split("\n");
        assertEquals(6, sentMessages.length, "Client should have sent exactly 6 lines before realizing server disconnected");
    }
}
