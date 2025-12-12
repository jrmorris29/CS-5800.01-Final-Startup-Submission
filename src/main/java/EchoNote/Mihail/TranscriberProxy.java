package EchoNote.Mihail;

import EchoNote.Jack.Transcript;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy Pattern: Caching proxy for Transcriber with logging and statistics.
 */
public class TranscriberProxy implements TranscriptionService {

    private final TranscriptionService realTranscriber;
    private final Map<String, Transcript> cache;
    private final boolean loggingEnabled;

    private int totalRequests;
    private int cacheHits;
    private int apiCalls;

    public TranscriberProxy(TranscriptionService realTranscriber, boolean loggingEnabled) {
        this.realTranscriber = realTranscriber;
        this.cache = new ConcurrentHashMap<>();
        this.loggingEnabled = loggingEnabled;
        this.totalRequests = 0;
        this.cacheHits = 0;
        this.apiCalls = 0;
    }

    public TranscriberProxy(TranscriptionService realTranscriber) {
        this(realTranscriber, true);
    }

    public TranscriberProxy() {
        this(new Transcriber(), true);
    }

    @Override
    public Transcript transcribe(Path audioFile) {
        totalRequests++;
        String cacheKey = getCacheKey(audioFile);
        
        log("Transcription requested for: " + audioFile.getFileName());

        if (cache.containsKey(cacheKey)) {
            cacheHits++;
            log("Cache HIT - returning cached transcript for: " + audioFile.getFileName());
            return cache.get(cacheKey);
        }

        log("Cache MISS - calling OpenAI API for: " + audioFile.getFileName());
        
        try {
            apiCalls++;
            Transcript transcript = realTranscriber.transcribe(audioFile);
            
            cache.put(cacheKey, transcript);
            log("Transcription successful, cached result for: " + audioFile.getFileName());
            
            return transcript;
        } catch (TranscriptionException e) {
            log("Transcription FAILED for: " + audioFile.getFileName() + " - " + e.getMessage());
            throw e;
        }
    }

    private String getCacheKey(Path audioFile) {
        try {
            long lastModified = java.nio.file.Files.getLastModifiedTime(audioFile).toMillis();
            return audioFile.toAbsolutePath().toString() + ":" + lastModified;
        } catch (Exception e) {
            return audioFile.toAbsolutePath().toString();
        }
    }

    private void log(String message) {
        if (loggingEnabled) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
            System.out.println("[TranscriberProxy " + timestamp + "] " + message);
        }
    }

    public void clearCache() {
        cache.clear();
        log("Cache cleared");
    }

    public int getCacheSize() {
        return cache.size();
    }

    public ProxyStats getStats() {
        return new ProxyStats(totalRequests, cacheHits, apiCalls);
    }

    /** Statistics record for proxy usage. */
    public record ProxyStats(int totalRequests, int cacheHits, int apiCalls) {
        public double cacheHitRate() {
            return totalRequests > 0 ? (double) cacheHits / totalRequests : 0.0;
        }
    }
}
