package EchoNote.Jack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Transcript {
    private final String id;
    private final String rawText;
    private final List<String> timestamps;
    private final TranscriptSource source;

    public Transcript(String id, String rawText, List<String> timestamps, TranscriptSource source) {
        this.id = id;
        this.rawText = rawText;
        this.timestamps = new ArrayList<>(timestamps);
        this.source = source;
    }

    public Transcript(String rawText, TranscriptSource source) {
        this(UUID.randomUUID().toString(), rawText, Collections.emptyList(), source);
    }

    public String getId() {
        return id;
    }

    public String getRawText() {
        return rawText;
    }

    public List<String> getTimestamps() {
        return Collections.unmodifiableList(timestamps);
    }

    public TranscriptSource getSource() {
        return source;
    }
}
