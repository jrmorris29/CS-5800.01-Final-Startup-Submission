package EchoNote.Arpit;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.MeetingRecordBuilder;
import EchoNote.Jack.Participant;
import EchoNote.Jack.Summary;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EmailNotifierTest {

    private MeetingRecord createRecordWithParticipants() {
        Participant arpit = new Participant("Arpit", "arpit@example.com", "Lead");
        Participant jack = new Participant("Jack", "jack@example.com", "Engineer");

        Summary summary = new Summary("sum-1");
        summary.setNotes("This is the summary sent by email.");

        ActionItem action = new ActionItem(
                "act-1",
                "Prepare follow-up report",
                arpit,
                LocalDate.now().plusDays(2)
        );

        return new MeetingRecordBuilder()
                .withTitle("Email Notification Test")
                .withDate(LocalDateTime.now())
                .withParticipants(List.of(arpit, jack))
                .withSummary(summary)
                .withActions(List.of(action))
                .build();
    }

    @Test
    void emailParticipants_nullRecord_throwsIllegalArgumentException() {
        EmailNotifier notifier = new EmailNotifier();

        assertThrows(
                IllegalArgumentException.class,
                () -> notifier.emailParticipants(null, "event-123"),
                "Passing null MeetingRecord should throw IllegalArgumentException"
        );
    }

    @Test
    void emailParticipants_simulatedFailure_throwsNotificationException() {
        MeetingRecord record = createRecordWithParticipants();
        EmailNotifier notifier = new EmailNotifier(true);

        assertThrows(
                NotificationException.class,
                () -> notifier.emailParticipants(record, "event-123"),
                "When simulateFailure is true, emailParticipants should throw NotificationException"
        );
    }

    @Test
    void emailParticipants_validRecord_doesNotThrow() {
        MeetingRecord record = createRecordWithParticipants();
        EmailNotifier notifier = new EmailNotifier(false);

        assertDoesNotThrow(
                () -> notifier.emailParticipants(record, "event-123"),
                "EmailNotifier should send emails (print to console) without throwing for valid participants"
        );
    }

    @Test
    void emailParticipants_noParticipants_doesNothing() {
        MeetingRecord record = new MeetingRecordBuilder()
                .withTitle("No Participants Meeting")
                .withDate(LocalDateTime.now())
                .build();

        EmailNotifier notifier = new EmailNotifier(false);

        assertDoesNotThrow(
                () -> notifier.emailParticipants(record, "event-123"),
                "If there are no participants, emailParticipants should simply return without error"
        );
    }
}
