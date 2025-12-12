package EchoNote.Arpit;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.ExportResult;
import EchoNote.Jack.MeetingFormatter;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.Summary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exports meeting records to HTML format.
 */
public class HtmlExporter implements MeetingExporter {

    private final File exportDirectory;

    public HtmlExporter(File exportDirectory) {
        this.exportDirectory = exportDirectory;
    }

    @Override
    public ExportResult export(MeetingRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");
        }

        if (!exportDirectory.exists() && !exportDirectory.mkdirs()) {
            return new ExportResult(
                    false,
                    null,
                    "Could not create export directory: " + exportDirectory.getAbsolutePath()
            );
        }

        String filename = "meeting-" + record.getId() + "." + getFileExtension();
        File outFile = new File(exportDirectory, filename);

        try (FileWriter writer = new FileWriter(outFile)) {
            writer.write(buildHtml(record));
            writer.flush();
        } catch (IOException e) {
            return new ExportResult(false, null, "Failed to write export file: " + e.getMessage());
        }

        return new ExportResult(true, outFile.getAbsolutePath(),
                "Exported to " + outFile.getAbsolutePath());
    }

    @Override
    public String getFileExtension() {
        return "html";
    }

    private String buildHtml(MeetingRecord record) {
        StringBuilder sb = new StringBuilder();
        String title = escapeHtml(MeetingFormatter.getTitleOrId(record));

        appendHtmlHeader(sb, title);
        appendHtmlBody(sb, record, title);

        return sb.toString();
    }

    private void appendHtmlHeader(StringBuilder sb, String title) {
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("    <title>Meeting: ").append(title).append("</title>\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; ");
        sb.append("max-width: 800px; margin: 0 auto; padding: 20px; line-height: 1.6; }\n");
        sb.append("        h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }\n");
        sb.append("        h2 { color: #555; margin-top: 30px; }\n");
        sb.append("        h3 { color: #666; }\n");
        sb.append("        .date { color: #888; font-size: 0.9em; }\n");
        sb.append("        ul { padding-left: 20px; }\n");
        sb.append("        li { margin: 5px 0; }\n");
        sb.append("        .action-item { background: #f8f9fa; padding: 10px; margin: 10px 0; ");
        sb.append("border-left: 3px solid #007bff; }\n");
        sb.append("        .owner { color: #28a745; }\n");
        sb.append("        .due-date { color: #dc3545; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
    }

    private void appendHtmlBody(StringBuilder sb, MeetingRecord record, String title) {
        sb.append("<body>\n");
        sb.append("    <h1>Meeting: ").append(title).append("</h1>\n");

        if (record.getDate() != null) {
            sb.append("    <p class=\"date\">Date: ").append(record.getDate()).append("</p>\n");
        }

        appendSummarySection(sb, record.getSummary());
        appendActionItemsSection(sb, record);

        sb.append("</body>\n");
        sb.append("</html>\n");
    }

    private void appendSummarySection(StringBuilder sb, Summary summary) {
        if (summary == null) {
            return;
        }

        sb.append("    <h2>Summary</h2>\n");

        if (summary.getNotes() != null && !summary.getNotes().isBlank()) {
            sb.append("    <p>").append(escapeHtml(summary.getNotes())).append("</p>\n");
        }

        if (!summary.getTopics().isEmpty()) {
            sb.append("    <h3>Topics</h3>\n");
            sb.append("    <ul>\n");
            for (String topic : summary.getTopics()) {
                sb.append("        <li>").append(escapeHtml(topic)).append("</li>\n");
            }
            sb.append("    </ul>\n");
        }

        if (!summary.getDecisions().isEmpty()) {
            sb.append("    <h3>Decisions</h3>\n");
            sb.append("    <ul>\n");
            for (String decision : summary.getDecisions()) {
                sb.append("        <li>").append(escapeHtml(decision)).append("</li>\n");
            }
            sb.append("    </ul>\n");
        }
    }

    private void appendActionItemsSection(StringBuilder sb, MeetingRecord record) {
        if (record.getActions().isEmpty()) {
            return;
        }

        sb.append("    <h2>Action Items</h2>\n");
        for (ActionItem item : record.getActions()) {
            sb.append("    <div class=\"action-item\">\n");
            sb.append("        <strong>").append(escapeHtml(item.getTitle())).append("</strong>");
            if (item.getOwner() != null) {
                sb.append(" <span class=\"owner\">(Owner: ")
                        .append(escapeHtml(item.getOwner().getName())).append(")</span>");
            }
            if (item.getDueDate() != null) {
                sb.append(" <span class=\"due-date\">[Due: ").append(item.getDueDate()).append("]</span>");
            }
            sb.append("\n    </div>\n");
        }
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

