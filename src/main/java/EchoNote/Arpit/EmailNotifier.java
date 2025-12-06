package EchoNote.Arpit;

import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.Participant;
import EchoNote.Jack.Summary;
import EchoNote.Jack.ActionItem;

import java.util.List;


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

        Summary summary = record.getSummary();
        List<ActionItem> actionItems = record.getActions();

        String subject = buildSubject(record, eventId);
        String body = buildBody(record, summary, actionItems);

        for (Participant p : participants) {
            if (p == null || p.getEmail() == null || p.getEmail().isBlank()) {
                continue;
            }
            System.out.println("=== Email to: " + p.getEmail() + " ===");
            System.out.println("Subject: " + subject);
            System.out.println();
            System.out.println(body);
            System.out.println("======================================");
        }
    }

    private String buildSubject(MeetingRecord record, String eventId) {
        String title = record.getTitle() != null ? record.getTitle() : "Meeting";
        if (eventId != null && !eventId.isBlank()) {
            return "[EchoNote] " + title + " (Event " + eventId + ")";
        }
        return "[EchoNote] " + title;
    }

    private String buildBody(MeetingRecord record, Summary summary, List<ActionItem> actionItems) {
        StringBuilder sb = new StringBuilder();

        String title = record.getTitle() != null ? record.getTitle() : "Meeting";
        sb.append("Hello,\n\n");
        sb.append("Here are the notes and action items for: ").append(title).append(".\n\n");

        if (summary != null) {
            sb.append("=== Summary ===\n");
            sb.append(summary.toString()).append("\n\n");
        }

        if (actionItems != null && !actionItems.isEmpty()) {
            sb.append("=== Action Items ===\n");
            for (ActionItem item : actionItems) {
                if (item != null) {
                    sb.append("- ").append(item.toString()).append("\n");
                }
            }
            sb.append("\n");
        } else {
            sb.append("No action items were recorded.\n\n");
        }

        sb.append("Best regards,\n");
        sb.append("EchoNote\n");
        return sb.toString();
    }
}
