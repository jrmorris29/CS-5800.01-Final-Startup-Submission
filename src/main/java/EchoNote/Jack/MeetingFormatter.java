package EchoNote.Jack;

import java.util.List;

/**
 * Utility class for formatting meeting record content.
 * Provides consistent text formatting for summaries and action items
 * across different output contexts (UI, email, export).
 */
public final class MeetingFormatter {

    private MeetingFormatter() {
        // Utility class - prevent instantiation
    }

    /**
     * Formats a summary as plain text with topics and decisions.
     */
    public static String formatSummaryAsText(Summary summary) {
        if (summary == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

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

        return sb.toString();
    }

    /**
     * Formats action items as plain text list.
     */
    public static String formatActionsAsText(List<ActionItem> actions) {
        if (actions == null || actions.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (ActionItem item : actions) {
            sb.append(formatActionItemAsText(item)).append("\n");
        }
        return sb.toString();
    }

    /**
     * Formats a single action item as plain text.
     */
    public static String formatActionItemAsText(ActionItem item) {
        if (item == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" - ").append(item.getTitle());

        if (item.getOwner() != null) {
            sb.append(" (Owner: ").append(item.getOwner().getName()).append(")");
        }

        if (item.getDueDate() != null) {
            sb.append(" [Due: ").append(item.getDueDate()).append("]");
        }

        return sb.toString();
    }

    /**
     * Returns the meeting title or a default value if null.
     */
    public static String getTitleOrDefault(MeetingRecord record) {
        if (record == null || record.getTitle() == null || record.getTitle().isBlank()) {
            return "Meeting";
        }
        return record.getTitle();
    }

    /**
     * Returns the meeting title or the ID as fallback.
     */
    public static String getTitleOrId(MeetingRecord record) {
        if (record == null) {
            return "Unknown";
        }
        if (record.getTitle() != null && !record.getTitle().isBlank()) {
            return record.getTitle();
        }
        return record.getId() != null ? record.getId().toString() : "Unknown";
    }
}
