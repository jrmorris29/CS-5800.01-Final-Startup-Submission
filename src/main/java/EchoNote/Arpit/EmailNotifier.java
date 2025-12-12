package EchoNote.Arpit;

import EchoNote.Jack.MeetingFormatter;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.Participant;

import java.util.List;

/**
 * Sends email notifications to meeting participants with summary and action items.
 */
public class EmailNotifier implements Notifier {

    private final boolean simulateFailure;

    public EmailNotifier() {
        this(false);
    }

    public EmailNotifier(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    @Override
    public void emailParticipants(MeetingRecord record, String eventId) throws NotificationException {
        if (record == null) {
            throw new IllegalArgumentException("record must not be null");
        }

        if (simulateFailure) {
            throw new NotificationException("Simulated email delivery failure.");
        }

        List<Participant> participants = record.getParticipants();
        if (participants == null || participants.isEmpty()) {
            return;
        }

        String subject = buildSubject(record, eventId);
        String body = buildBody(record);

        sendToAllParticipants(participants, subject, body);
    }

    private void sendToAllParticipants(List<Participant> participants, String subject, String body) {
        for (Participant participant : participants) {
            if (hasValidEmail(participant)) {
                printEmail(participant.getEmail(), subject, body);
            }
        }
    }

    private boolean hasValidEmail(Participant participant) {
        return participant != null
                && participant.getEmail() != null
                && !participant.getEmail().isBlank();
    }

    private void printEmail(String email, String subject, String body) {
        System.out.println("=== Email to: " + email + " ===");
        System.out.println("Subject: " + subject);
        System.out.println();
        System.out.println(body);
        System.out.println("======================================");
    }

    private String buildSubject(MeetingRecord record, String eventId) {
        String title = MeetingFormatter.getTitleOrDefault(record);
        if (eventId != null && !eventId.isBlank()) {
            return "[EchoNote] " + title + " (Event " + eventId + ")";
        }
        return "[EchoNote] " + title;
    }

    private String buildBody(MeetingRecord record) {
        StringBuilder sb = new StringBuilder();

        String title = MeetingFormatter.getTitleOrDefault(record);
        sb.append("Hello,\n\n");
        sb.append("Here are the notes and action items for: ").append(title).append(".\n\n");

        if (record.getSummary() != null) {
            sb.append("=== Summary ===\n");
            sb.append(record.getSummary().toString()).append("\n\n");
        }

        if (!record.getActions().isEmpty()) {
            sb.append("=== Action Items ===\n");
            sb.append(MeetingFormatter.formatActionsAsText(record.getActions()));
            sb.append("\n");
        } else {
            sb.append("No action items were recorded.\n\n");
        }

        sb.append("Best regards,\n");
        sb.append("EchoNote\n");
        return sb.toString();
    }
}
