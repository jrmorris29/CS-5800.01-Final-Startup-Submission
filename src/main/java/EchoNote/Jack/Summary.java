package EchoNote.Jack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Summary {
    private final String id;
    private final List<String> topics = new ArrayList<>();
    private final List<String> decisions = new ArrayList<>();
    private String notes;

    public Summary(String id) {
        this.id = id;
    }

    public Summary(List<String> topics, List<String> decisions, String notes) {
        this(UUID.randomUUID().toString());
        if (topics != null) {
            this.topics.addAll(topics);
        }
        if (decisions != null) {
            this.decisions.addAll(decisions);
        }
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public List<String> getTopics() {
        return Collections.unmodifiableList(topics);
    }

    public List<String> getDecisions() {
        return Collections.unmodifiableList(decisions);
    }

    public void addTopic(String topic) {
        topics.add(topic);
    }

    public void addDecision(String decision) {
        decisions.add(decision);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
