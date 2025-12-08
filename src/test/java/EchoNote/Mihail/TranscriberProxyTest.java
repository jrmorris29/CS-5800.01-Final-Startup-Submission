package EchoNote.Mihail;

import EchoNote.Jack.Transcript;
import EchoNote.Jack.TranscriptSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for TranscriberProxy: Proxy pattern. */
public class TranscriberProxyTest {

    @TempDir
    Path tempDir;

    private Path testAudioFile;
    private MockTranscriptionService mockTranscriber;
    private TranscriberProxy proxy;

    @BeforeEach
    void setUp() throws IOException {
        testAudioFile = tempDir.resolve("test-audio.wav");
        Files.writeString(testAudioFile, "fake audio content");
        mockTranscriber = new MockTranscriptionService();
        proxy = new TranscriberProxy(mockTranscriber, false);
    }

    static class MockTranscriptionService implements TranscriptionService {
        int callCount = 0;
        Transcript resultToReturn = new Transcript("Mock transcription", TranscriptSource.LIVE);

        @Override
        public Transcript transcribe(Path audioFile) {
            callCount++;
            return resultToReturn;
        }
    }

    @Nested
    @DisplayName("Proxy Delegation Tests")
    class DelegationTests {

        @Test
        @DisplayName("Proxy delegates to real transcriber on first call")
        void proxy_delegatesToRealTranscriber() {
            Transcript result = proxy.transcribe(testAudioFile);

            assertNotNull(result);
            assertEquals("Mock transcription", result.getRawText());
            assertEquals(1, mockTranscriber.callCount, 
                    "Real transcriber should be called once");
        }

        @Test
        @DisplayName("Proxy implements same interface as real subject")
        void proxy_implementsSameInterface() {
            TranscriptionService realService = new Transcriber("fake-key");
            TranscriptionService proxyService = new TranscriberProxy(mockTranscriber);
            assertTrue(realService instanceof TranscriptionService);
            assertTrue(proxyService instanceof TranscriptionService);
        }
    }

    @Nested
    @DisplayName("Caching Behavior Tests")
    class CachingTests {

        @Test
        @DisplayName("Same file returns cached result on second call")
        void sameFile_returnsCachedResult() {
            // First call - should hit the real transcriber
            Transcript first = proxy.transcribe(testAudioFile);
            assertEquals(1, mockTranscriber.callCount);

            Transcript second = proxy.transcribe(testAudioFile);
            assertEquals(1, mockTranscriber.callCount, "Should not call again");
            assertSame(first, second, "Should return cached transcript");
        }

        @Test
        @DisplayName("Different files are cached separately")
        void differentFiles_cachedSeparately() throws IOException {
            Path anotherFile = tempDir.resolve("another-audio.wav");
            Files.writeString(anotherFile, "different content");

            proxy.transcribe(testAudioFile);
            assertEquals(1, mockTranscriber.callCount);

            proxy.transcribe(anotherFile);
            assertEquals(2, mockTranscriber.callCount, "Should trigger new call");
            assertEquals(2, proxy.getCacheSize(), "Cache should have both");
        }

        @Test
        @DisplayName("clearCache removes all cached entries")
        void clearCache_removesAllEntries() {
            proxy.transcribe(testAudioFile);
            assertEquals(1, proxy.getCacheSize());

            proxy.clearCache();

            assertEquals(0, proxy.getCacheSize());

            proxy.transcribe(testAudioFile);
            assertEquals(2, mockTranscriber.callCount, "Should call again after clear");
        }
    }

    @Nested
    @DisplayName("Statistics Tracking Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Stats track total requests, cache hits, and API calls")
        void stats_trackAllMetrics() {
            proxy.transcribe(testAudioFile);
            proxy.transcribe(testAudioFile);
            proxy.transcribe(testAudioFile);

            TranscriberProxy.ProxyStats stats = proxy.getStats();

            assertEquals(3, stats.totalRequests());
            assertEquals(2, stats.cacheHits());
            assertEquals(1, stats.apiCalls());
            assertEquals(2.0 / 3.0, stats.cacheHitRate(), 0.01);
        }
    }
}

