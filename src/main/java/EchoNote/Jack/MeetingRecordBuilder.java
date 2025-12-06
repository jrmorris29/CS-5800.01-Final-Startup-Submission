package EchoNote.Jack;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MeetingRecordBuilder {
    private final MeetingRecord record;
    private final List<String> tags = new ArrayList<>();
    private final List<Participant> participants = new ArrayList<>();
    private List<ActionItem> actions = new ArrayList<>();

    public MeetingRecordBuilder() {
        this.record = new MeetingRecord();
    }

    public MeetingRecordBuilder withTitle(String title) {
        record.setTitle(title);
        return this;
    }

    public MeetingRecordBuilder withTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
        return this;
    }

    public MeetingRecordBuilder withDate(LocalDateTime date) {
        record.setDate(date);
        return this;
    }

    public MeetingRecordBuilder withParticipants(List<Participant> participants) {
        this.participants.clear();
        this.participants.addAll(participants);
        return this;
    }

    public MeetingRecordBuilder withTranscript(Transcript transcript) {
        record.setTranscript(transcript);
        return this;
    }

    public MeetingRecordBuilder withSummary(Summary summary) {
        record.setSummary(summary);
        return this;
    }

    public MeetingRecordBuilder withActions(List<ActionItem> actions) {
        this.actions = new ArrayList<>(actions);
        return this;
    }

    public MeetingRecord build() {
        record.setTags(tags);
        record.setParticipants(participants);
        record.setActions(actions);
        return record;
    }
}
