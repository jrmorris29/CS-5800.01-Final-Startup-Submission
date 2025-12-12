package EchoNote.Jack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for serializing MeetingRecord to/from JSON.
 * Flattens nested objects for clean JSON representation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeetingRecordDto {

    public String id;
    public String title;
    public List<String> tags;
    public LocalDateTime date;
    public String status;
    public List<ParticipantDto> participants;
    public TranscriptDto transcript;
    public SummaryDto summary;
    public List<ActionItemDto> actions;
    public String audioFilePath;

    public MeetingRecordDto() {
        this.tags = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    public static MeetingRecordDto fromMeetingRecord(MeetingRecord record) {
        MeetingRecordDto dto = new MeetingRecordDto();
        dto.id = record.getId().toString();
        dto.title = record.getTitle();
        dto.tags = new ArrayList<>(record.getTags());
        dto.date = record.getDate();
        dto.status = record.getStatus() != null ? record.getStatus().name() : null;
        dto.audioFilePath = record.getAudioFilePath();

        dto.participants = record.getParticipants().stream()
                .map(ParticipantDto::fromParticipant)
                .toList();

        if (record.getTranscript() != null) {
            dto.transcript = TranscriptDto.fromTranscript(record.getTranscript());
        }

        if (record.getSummary() != null) {
            dto.summary = SummaryDto.fromSummary(record.getSummary());
        }

        dto.actions = record.getActions().stream()
                .map(ActionItemDto::fromActionItem)
                .toList();

        return dto;
    }

    public MeetingRecord toMeetingRecord() {
        MeetingRecord record = new MeetingRecordBuilder()
                .withId(UUID.fromString(this.id))
                .withTitle(this.title)
                .withDate(this.date)
                .build();
        record.setTags(this.tags);
        record.setAudioFilePath(this.audioFilePath);

        if (this.status != null) {
            record.setStatus(ApprovalStatus.valueOf(this.status));
        }

        if (this.participants != null) {
            record.setParticipants(
                    this.participants.stream()
                            .map(ParticipantDto::toParticipant)
                            .toList()
            );
        }

        if (this.transcript != null) {
            record.setTranscript(this.transcript.toTranscript());
        }

        if (this.summary != null) {
            record.setSummary(this.summary.toSummary());
        }

        if (this.actions != null) {
            record.setActions(
                    this.actions.stream()
                            .map(ActionItemDto::toActionItem)
                            .toList()
            );
        }

        return record;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParticipantDto {
        public String name;
        public String email;
        public String role;

        public ParticipantDto() {}

        public static ParticipantDto fromParticipant(Participant p) {
            ParticipantDto dto = new ParticipantDto();
            dto.name = p.getName();
            dto.email = p.getEmail();
            dto.role = p.getRole();
            return dto;
        }

        public Participant toParticipant() {
            return new Participant(name, email, role);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TranscriptDto {
        public String id;
        public String rawText;
        public List<String> timestamps;
        public String source;

        public TranscriptDto() {
            this.timestamps = new ArrayList<>();
        }

        public static TranscriptDto fromTranscript(Transcript t) {
            TranscriptDto dto = new TranscriptDto();
            dto.id = t.getId();
            dto.rawText = t.getRawText();
            dto.timestamps = new ArrayList<>(t.getTimestamps());
            dto.source = t.getSource() != null ? t.getSource().name() : null;
            return dto;
        }

        public Transcript toTranscript() {
            TranscriptSource src = source != null ? TranscriptSource.valueOf(source) : TranscriptSource.IMPORTED;
            return new Transcript(id, rawText, timestamps != null ? timestamps : List.of(), src);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SummaryDto {
        public String id;
        public List<String> topics;
        public List<String> decisions;
        public String notes;

        public SummaryDto() {
            this.topics = new ArrayList<>();
            this.decisions = new ArrayList<>();
        }

        public static SummaryDto fromSummary(Summary s) {
            SummaryDto dto = new SummaryDto();
            dto.id = s.getId();
            dto.topics = new ArrayList<>(s.getTopics());
            dto.decisions = new ArrayList<>(s.getDecisions());
            dto.notes = s.getNotes();
            return dto;
        }

        public Summary toSummary() {
            return new Summary(topics, decisions, notes);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActionItemDto {
        public String id;
        public String title;
        public ParticipantDto owner;
        public LocalDate dueDate;
        public String status;

        public ActionItemDto() {}

        public static ActionItemDto fromActionItem(ActionItem a) {
            ActionItemDto dto = new ActionItemDto();
            dto.id = a.getId();
            dto.title = a.getTitle();
            if (a.getOwner() != null) {
                dto.owner = ParticipantDto.fromParticipant(a.getOwner());
            }
            dto.dueDate = a.getDueDate();
            dto.status = a.getStatus() != null ? a.getStatus().name() : null;
            return dto;
        }

        public ActionItem toActionItem() {
            Participant ownerObj = owner != null ? owner.toParticipant() : null;
            ActionItem item = new ActionItem(id, title, ownerObj, dueDate);
            if (status != null) {
                item.setStatus(ActionStatus.valueOf(status));
            }
            return item;
        }
    }
}
