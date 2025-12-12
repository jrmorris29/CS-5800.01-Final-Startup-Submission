package EchoNote.Mihail;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.InvalidEmailException;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.MeetingRecordBuilder;
import EchoNote.Jack.Participant;
import EchoNote.Jack.Summary;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EmailDraftServiceTest {

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
                .withTitle("Email Draft Test")
                .withDate(LocalDateTime.now())
                .withParticipants(List.of(arpit, jack))
                .withSummary(summary)
                .withActions(List.of(action))
                .build();
    }

    // --- Tests for Email Validation ---

    @Test
    void validateEmail_nullEmail_throwsInvalidEmailException() {
        EmailDraftService service = new EmailDraftService();

        assertThrows(
                InvalidEmailException.class,
                () -> service.validateEmail(null),
                "Null email should throw InvalidEmailException"
        );
    }

    @Test
    void validateEmail_blankEmail_throwsInvalidEmailException() {
        EmailDraftService service = new EmailDraftService();

        assertThrows(
                InvalidEmailException.class,
                () -> service.validateEmail("   "),
                "Blank email should throw InvalidEmailException"
        );
    }

    @Test
    void validateEmail_invalidFormat_throwsInvalidEmailException() {
        EmailDraftService service = new EmailDraftService();

        assertThrows(
                InvalidEmailException.class,
                () -> service.validateEmail("not-an-email"),
                "Invalid email format should throw InvalidEmailException"
        );
    }

    @Test
    void validateEmail_validEmail_doesNotThrow() {
        EmailDraftService service = new EmailDraftService();

        assertDoesNotThrow(
                () -> service.validateEmail("valid@example.com"),
                "Valid email should not throw"
        );
    }

    @Test
    void isValidEmail_validEmails_returnsTrue() {
        EmailDraftService service = new EmailDraftService();

        assertTrue(service.isValidEmail("test@example.com"));
        assertTrue(service.isValidEmail("user.name@domain.org"));
        assertTrue(service.isValidEmail("user+tag@example.co.uk"));
    }

    @Test
    void isValidEmail_invalidEmails_returnsFalse() {
        EmailDraftService service = new EmailDraftService();

        assertFalse(service.isValidEmail(null));
        assertFalse(service.isValidEmail(""));
        assertFalse(service.isValidEmail("   "));
        assertFalse(service.isValidEmail("not-an-email"));
        assertFalse(service.isValidEmail("missing@"));
        assertFalse(service.isValidEmail("@missing-local.com"));
    }

    // --- Tests for Draft Subject and Body ---

    @Test
    void buildDraftSubject_includesMeetingTitle() {
        EmailDraftService service = new EmailDraftService();
        MeetingRecord record = createRecordWithParticipants();

        String subject = service.buildDraftSubject(record);

        assertTrue(subject.contains("EchoNote Summary"));
        assertTrue(subject.contains("Email Draft Test"));
    }

    @Test
    void buildDraftSubject_nullTitle_usesDefaultTitle() {
        EmailDraftService service = new EmailDraftService();
        MeetingRecord record = new MeetingRecordBuilder()
                .withDate(LocalDateTime.now())
                .build();

        String subject = service.buildDraftSubject(record);

        assertTrue(subject.contains("EchoNote Summary"));
        assertTrue(subject.contains("Meeting"));
    }

    @Test
    void buildDraftBody_includesSummaryAndActions() {
        EmailDraftService service = new EmailDraftService();
        MeetingRecord record = createRecordWithParticipants();

        String body = service.buildDraftBody(record);

        assertTrue(body.contains("Hello"), "Body should start with greeting");
        assertTrue(body.contains("Email Draft Test"), "Body should include title");
        assertTrue(body.contains("SUMMARY"), "Body should have summary section");
        assertTrue(body.contains("This is the summary sent by email."), "Body should include summary notes");
        assertTrue(body.contains("ACTION ITEMS"), "Body should have action items section");
        assertTrue(body.contains("Prepare follow-up report"), "Body should include action item title");
        assertTrue(body.contains("Owner: Arpit"), "Body should include action item owner");
        assertTrue(body.contains("Best regards"), "Body should have sign-off");
    }

    @Test
    void buildDraftBody_noSummary_indicatesNoSummary() {
        EmailDraftService service = new EmailDraftService();
        MeetingRecord record = new MeetingRecordBuilder()
                .withTitle("No Summary Meeting")
                .withDate(LocalDateTime.now())
                .build();

        String body = service.buildDraftBody(record);

        assertTrue(body.contains("No summary available"), "Body should indicate no summary");
    }

    // --- Tests for openEmailDraft ---

    @Test
    void openEmailDraft_nullRecord_throwsIllegalArgumentException() {
        EmailDraftService service = new EmailDraftService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.openEmailDraft(null, "test@example.com"),
                "Null record should throw IllegalArgumentException"
        );
    }

    @Test
    void openEmailDraft_invalidEmail_throwsInvalidEmailException() {
        EmailDraftService service = new EmailDraftService();
        MeetingRecord record = createRecordWithParticipants();

        assertThrows(
                InvalidEmailException.class,
                () -> service.openEmailDraft(record, "invalid-email"),
                "Invalid email should throw InvalidEmailException"
        );
    }
}
