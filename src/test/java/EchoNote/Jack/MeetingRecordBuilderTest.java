package EchoNote.Jack;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MeetingRecordBuilderTest {

    @Test
    void build_populatesAllFieldsCorrectly() {
        Participant mihail = new Participant("Mihail", "mihail@example.com", "Engineer");
        Participant arpit = new Participant("Arpit", "arpit@example.com", "Lead");

        Summary summary = new Summary("summary-1");
        summary.addTopic("Architecture");
        summary.addDecision("Use OpenAI integration");
        summary.setNotes("Design discussion for EchoNote.");

        LocalDateTime meetingDate = LocalDateTime.of(2025, 1, 10, 14, 30);

        List<String> tags = Arrays.asList("echo", "note");
        List<Participant> participants = Arrays.asList(mihail, arpit);

        ActionItem action = new ActionItem(
                "action-1",
                "Prepare design document",
                arpit,
                LocalDate.of(2025, 1, 15)
        );
        List<ActionItem> actions = Arrays.asList(action);

        MeetingRecord record = new MeetingRecordBuilder()
                .withTitle("Client Kickoff")
                .withDate(meetingDate)
                .withTags(tags)
                .withParticipants(participants)
                .withSummary(summary)
                .withActions(actions)
                .build();

        assertNotNull(record.getId(), "MeetingRecord id should not be null");
        assertEquals("Client Kickoff", record.getTitle());
        assertEquals(meetingDate, record.getDate());

        assertEquals(ApprovalStatus.DRAFT, record.getStatus(),
                "New MeetingRecord built via MeetingRecordBuilder should default to DRAFT status");

        assertEquals(tags, record.getTags(), "Tags should match what was passed to the builder");
        assertEquals(participants, record.getParticipants(), "Participants should match what was passed to the builder");
        assertSame(summary, record.getSummary(), "Summary should be the same object passed to the builder");
        assertEquals(actions, record.getActions(), "Actions should match what was passed to the builder");
    }

    @Test
    void builtRecordHasUnmodifiableCollections() {
        MeetingRecord record = new MeetingRecordBuilder()
                .withTitle("Immutable Lists Test")
                .build();

        List<String> tags = record.getTags();
        List<Participant> participants = record.getParticipants();
        List<ActionItem> actions = record.getActions();

        assertThrows(UnsupportedOperationException.class,
                () -> tags.add("new-tag"),
                "Tags list from MeetingRecord should be unmodifiable");

        assertThrows(UnsupportedOperationException.class,
                () -> participants.add(new Participant("Mihail", "mihail@example.com", "Engineer")),
                "Participants list from MeetingRecord should be unmodifiable");

        assertThrows(UnsupportedOperationException.class,
                () -> actions.add(new ActionItem(
                        "tmp",
                        "Temporary",
                        new Participant("Arpit", "arpit@example.com", "Lead"),
                        LocalDate.now())),
                "Actions list from MeetingRecord should be unmodifiable");
    }

    @Test
    void defaultStatusIsDraft() {
        MeetingRecord record = new MeetingRecordBuilder()
                .withTitle("Status Test")
                .build();

        assertEquals(ApprovalStatus.DRAFT, record.getStatus(),
                "New MeetingRecord should default to DRAFT status");
    }
}
