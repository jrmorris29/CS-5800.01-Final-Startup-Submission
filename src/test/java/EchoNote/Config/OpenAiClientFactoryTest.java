package EchoNote.Config;

import com.openai.client.OpenAIClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for OpenAiClientFactory: Singleton and Factory patterns. */
public class OpenAiClientFactoryTest {

    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonPatternTests {

        @Test
        @DisplayName("getClient() returns a non-null instance")
        void getClient_returnsNonNullInstance() {
            OpenAIClient client = OpenAiClientFactory.getClient();
            
            assertNotNull(client, "Factory should return a non-null OpenAIClient instance");
        }

        @Test
        @DisplayName("getClient() returns the same instance on multiple calls (Singleton)")
        void getClient_returnsSameInstance_onMultipleCalls() {
            OpenAIClient firstCall = OpenAiClientFactory.getClient();
            OpenAIClient secondCall = OpenAiClientFactory.getClient();
            OpenAIClient thirdCall = OpenAiClientFactory.getClient();
            
            assertSame(firstCall, secondCall, 
                    "Singleton pattern: First and second calls should return the same instance");
            assertSame(secondCall, thirdCall, 
                    "Singleton pattern: Second and third calls should return the same instance");
            assertSame(firstCall, thirdCall, 
                    "Singleton pattern: All calls should return the same instance");
        }

        @Test
        @DisplayName("getClient() returns consistent instance across different threads")
        void getClient_returnsSameInstance_acrossThreads() throws InterruptedException {
            final OpenAIClient[] clients = new OpenAIClient[3];
            
            Thread t1 = new Thread(() -> clients[0] = OpenAiClientFactory.getClient());
            Thread t2 = new Thread(() -> clients[1] = OpenAiClientFactory.getClient());
            Thread t3 = new Thread(() -> clients[2] = OpenAiClientFactory.getClient());
            
            t1.start();
            t2.start();
            t3.start();
            
            t1.join();
            t2.join();
            t3.join();
            
            assertNotNull(clients[0], "Thread 1 should get a non-null client");
            assertNotNull(clients[1], "Thread 2 should get a non-null client");
            assertNotNull(clients[2], "Thread 3 should get a non-null client");
            
            assertSame(clients[0], clients[1], 
                    "Singleton pattern: Clients from different threads should be the same instance");
            assertSame(clients[1], clients[2], 
                    "Singleton pattern: All clients from different threads should be the same instance");
        }
    }

    @Nested
    @DisplayName("Factory Pattern Tests")
    class FactoryPatternTests {

        @Test
        @DisplayName("Factory creates a properly configured OpenAIClient")
        void factory_createsProperlyConfiguredClient() {
            OpenAIClient client = OpenAiClientFactory.getClient();
            
            assertNotNull(client, "Factory should create a non-null client");
            assertDoesNotThrow(() -> client.audio(), 
                    "Factory-created client should have audio() method accessible");
            assertDoesNotThrow(() -> client.chat(), 
                    "Factory-created client should have chat() method accessible");
        }

        @Test
        @DisplayName("Factory encapsulates API key configuration")
        void factory_encapsulatesApiKeyConfiguration() {
            OpenAIClient client = OpenAiClientFactory.getClient();
            assertNotNull(client, "Factory should encapsulate configuration");
        }

        @Test
        @DisplayName("Factory returns OpenAIClient interface type")
        void factory_returnsInterfaceType() {
            OpenAIClient client = OpenAiClientFactory.getClient();
            assertTrue(client instanceof OpenAIClient, "Should return OpenAIClient interface");
        }
    }
}
