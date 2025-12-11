package EchoNote.Mihail;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.InvalidEmailException;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.Summary;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Service for creating and opening email drafts with meeting summaries.
 * Opens the user's default email client with a prefilled draft.
 */
public class EmailDraftService {

    // Simple email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    /**
     * Opens the user's default email client with a prefilled draft containing
     * the meeting summary.
     *
     * @param record         the meeting record to summarize
     * @param recipientEmail the recipient's email address
     * @throws InvalidEmailException if the email address is invalid
     * @throws EmailDraftException   if the mail client cannot be opened
     */
    public void openEmailDraft(MeetingRecord record, String recipientEmail)
            throws InvalidEmailException, EmailDraftException {
        if (record == null) {
            throw new IllegalArgumentException("record must not be null");
        }

        validateEmail(recipientEmail);

        String subject = buildDraftSubject(record);
        String body = buildDraftBody(record);

        openMailClient(recipientEmail, subject, body);
    }

    /**
     * Validates an email address format.
     *
     * @param email the email address to validate
     * @throws InvalidEmailException if the email is null, blank, or invalid format
     */
    public void validateEmail(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            throw new InvalidEmailException("Email address cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidEmailException("Invalid email address format: " + email);
        }
    }

    /**
     * Checks if an email address is valid without throwing an exception.
     *
     * @param email the email address to check
     * @return true if the email is valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Returns the formatted subject line for an email draft.
     */
    public String buildDraftSubject(MeetingRecord record) {
        String title = record.getTitle() != null ? record.getTitle() : "Meeting";
        return "EchoNote Summary - " + title;
    }

    /**
     * Returns the formatted body text for an email draft, similar to the Details panel format.
     */
    public String buildDraftBody(MeetingRecord record) {
        StringBuilder sb = new StringBuilder();

        String title = record.getTitle() != null ? record.getTitle() : "Meeting";
        sb.append("Hello,\n\n");
        sb.append("Here is the summary for: ").append(title).append("\n\n");

        Summary summary = record.getSummary();
        if (summary != null) {
            sb.append("--- SUMMARY ---\n");
            if (summary.getNotes() != null && !summary.getNotes().isBlank()) {
                sb.append(summary.getNotes()).append("\n\n");
            }
            if (!summary.getTopics().isEmpty()) {
                sb.append("Topics:\n");
                for (String topic : summary.getTopics()) {
                    sb.append(" - ").append(topic).append("\n");
                }
                sb.append("\n");
            }
            if (!summary.getDecisions().isEmpty()) {
                sb.append("Decisions:\n");
                for (String decision : summary.getDecisions()) {
                    sb.append(" - ").append(decision).append("\n");
                }
                sb.append("\n");
            }
        } else {
            sb.append("No summary available for this meeting.\n\n");
        }

        List<ActionItem> actionItems = record.getActions();
        if (actionItems != null && !actionItems.isEmpty()) {
            sb.append("--- ACTION ITEMS ---\n");
            for (ActionItem item : actionItems) {
                if (item != null) {
                    sb.append(" - ").append(item.getTitle());
                    if (item.getOwner() != null) {
                        sb.append(" (Owner: ").append(item.getOwner().getName()).append(")");
                    }
                    if (item.getDueDate() != null) {
                        sb.append(" [Due: ").append(item.getDueDate()).append("]");
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("Best regards,\n");
        sb.append("EchoNote\n");

        return sb.toString();
    }

    /**
     * Opens the default mail client with a mailto URI.
     */
    private void openMailClient(String to, String subject, String body) throws EmailDraftException {
        if (!Desktop.isDesktopSupported()) {
            throw new EmailDraftException("Desktop is not supported on this system.");
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.MAIL)) {
            throw new EmailDraftException("Mail action is not supported on this system.");
        }

        try {
            String encodedSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8).replace("+", "%20");
            String encodedBody = URLEncoder.encode(body, StandardCharsets.UTF_8).replace("+", "%20");

            // Limit body length to avoid mailto URI size issues (most clients support ~2000 chars)
            if (encodedBody.length() > 1800) {
                encodedBody = encodedBody.substring(0, 1800) + URLEncoder.encode("...\n[Content truncated]", StandardCharsets.UTF_8).replace("+", "%20");
            }

            String mailtoUri = String.format("mailto:%s?subject=%s&body=%s",
                    URLEncoder.encode(to.trim(), StandardCharsets.UTF_8),
                    encodedSubject,
                    encodedBody);

            desktop.mail(new URI(mailtoUri));
        } catch (Exception e) {
            throw new EmailDraftException("Failed to open email client: " + e.getMessage());
        }
    }
}

