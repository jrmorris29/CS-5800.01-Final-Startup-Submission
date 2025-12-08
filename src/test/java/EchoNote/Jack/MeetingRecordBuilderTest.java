package EchoNote.Jack;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for MeetingRecordBuilder: Builder pattern. */
public class MeetingRecordBuilderTest {

    @Nested
    @DisplayName("Builder Pattern - Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("Builder methods return 'this' for method chaining")
        void builderMethods_returnThis_forMethodChaining() {
            MeetingRecordBuilder builder = new MeetingRecordBuilder();

            // Each method should return the same builder instance
            MeetingRecordBuilder afterTitle = builder.withTitle("Test");
            assertSame(builder, afterTitle, "withTitle() should return same builder instance");

            MeetingRecordBuilder afterDate = builder.withDate(LocalDateTime.now());
            assertSame(builder, afterDate, "withDate() should return same builder instance");

            MeetingRecordBuilder afterTags = builder.withTags(Arrays.asList("tag1"));
            assertSame(builder, afterTags, "withTags() should return same builder instance");

            MeetingRecordBuilder afterParticipants = builder.withParticipants(Arrays.asList());
            assertSame(builder, afterParticipants, "withParticipants() should return same builder instance");

            MeetingRecordBuilder afterSummary = builder.withSummary(new Summary("id"));
            assertSame(builder, afterSummary, "withSummary() should return same builder instance");

            MeetingRecordBuilder afterActions = builder.withActions(Arrays.asList());
            assertSame(builder, afterActions, "withActions() should return same builder instance");
        }

        @Test
        @DisplayName("Builder supports method chaining in any order")
        void builder_supportsMethodChaining_inAnyOrder() {
            // Build with one order
            MeetingRecord record1 = new MeetingRecordBuilder()
                    .withTitle("Meeting A")
                    .withDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                    .withTags(Arrays.asList("urgent"))
                    .build();

            // Build with different order - should work the same
            MeetingRecord record2 = new MeetingRecordBuilder()
                    .withTags(Arrays.asList("urgent"))
                    .withDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                    .withTitle("Meeting A")
                    .build();

            assertEquals(record1.getTitle(), record2.getTitle());
            assertEquals(record1.getDate(), record2.getDate());
            assertEquals(record1.getTags(), record2.getTags());
        }
    }

    @Nested
    @DisplayName("Builder Pattern - Optional Fields Tests")
    class OptionalFieldsTests {

        @Test
        @DisplayName("Builder can create object with only required fields")
        void builder_canCreateObject_withMinimalFields() {
            // Builder allows creating object with just title
            MeetingRecord record = new MeetingRecordBuilder()
                    .withTitle("Minimal Meeting")
                    .build();

            assertNotNull(record);
            assertEquals("Minimal Meeting", record.getTitle());
            assertNotNull(record.getId());
            assertTrue(record.getTags().isEmpty());
            assertTrue(record.getParticipants().isEmpty());
        }

        @Test
        @DisplayName("Builder can create object with all fields")
        void builder_canCreateObject_withAllFields() {
            Participant participant = new Participant("John", "john@example.com", "Developer");
            Summary summary = new Summary("summary-id");
            Transcript transcript = new Transcript("transcript text", TranscriptSource.LIVE);
            ActionItem action = new ActionItem("action-1", "Task", participant, LocalDate.now());

            MeetingRecord record = new MeetingRecordBuilder()
                    .withTitle("Full Meeting")
                    .withDate(LocalDateTime.now())
                    .withTags(Arrays.asList("important", "quarterly"))
                    .withParticipants(Arrays.asList(participant))
                    .withSummary(summary)
                    .withTranscript(transcript)
                    .withActions(Arrays.asList(action))
                    .build();

            assertNotNull(record);
            assertEquals("Full Meeting", record.getTitle());
            assertEquals(2, record.getTags().size());
            assertEquals(1, record.getParticipants().size());
            assertSame(summary, record.getSummary());
            assertSame(transcript, record.getTranscript());
            assertEquals(1, record.getActions().size());
        }
    }

    @Nested
    @DisplayName("Builder Pattern - Different Representations Tests")
    class DifferentRepresentationsTests {

        @Test
        @DisplayName("Same builder process creates unique objects")
        void sameBuilderProcess_createsUniqueObjects() {
            MeetingRecordBuilder builder = new MeetingRecordBuilder()
                    .withTitle("Template Meeting")
                    .withTags(Arrays.asList("template"));

            MeetingRecord record1 = builder.build();

            // Create a new builder with same setup
            MeetingRecord record2 = new MeetingRecordBuilder()
                    .withTitle("Template Meeting")
                    .withTags(Arrays.asList("template"))
                    .build();

            // Should be different objects with different IDs
            assertNotSame(record1, record2);
            assertNotEquals(record1.getId(), record2.getId());

            // But same content
            assertEquals(record1.getTitle(), record2.getTitle());
            assertEquals(record1.getTags(), record2.getTags());
        }
    }

    @Nested
    @DisplayName("Original Tests - Functionality Verification")
    class OriginalTests {

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
}
