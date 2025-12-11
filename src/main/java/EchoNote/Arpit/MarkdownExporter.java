package EchoNote.Arpit;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.ExportResult;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.Summary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exports meeting records to Markdown format.
 */
public class MarkdownExporter implements MeetingExporter {

    private final File exportDirectory;

    public MarkdownExporter(File exportDirectory) {
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
            writer.write(buildMarkdown(record));
            writer.flush();
        } catch (IOException e) {
            return new ExportResult(false, null, "Failed to write export file: " + e.getMessage());
        }

        return new ExportResult(true, outFile.getAbsolutePath(),
                "Exported to " + outFile.getAbsolutePath());
    }

    @Override
    public String getFileExtension() {
        return "md";
    }

    private String buildMarkdown(MeetingRecord record) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Meeting ")
                .append(record.getTitle() != null ? record.getTitle() : record.getId())
                .append("\n\n");

        if (record.getDate() != null) {
            sb.append("Date: ").append(record.getDate()).append("\n\n");
        }

        Summary summary = record.getSummary();
        if (summary != null) {
            sb.append("## Summary\n\n");
            if (summary.getNotes() != null) {
                sb.append(summary.getNotes()).append("\n\n");
            }
            if (!summary.getTopics().isEmpty()) {
                sb.append("### Topics\n");
                for (String topic : summary.getTopics()) {
                    sb.append("- ").append(topic).append("\n");
                }
                sb.append("\n");
            }
            if (!summary.getDecisions().isEmpty()) {
                sb.append("### Decisions\n");
                for (String decision : summary.getDecisions()) {
                    sb.append("- ").append(decision).append("\n");
                }
                sb.append("\n");
            }
        }

        if (!record.getActions().isEmpty()) {
            sb.append("## Action Items\n");
            for (ActionItem item : record.getActions()) {
                sb.append("- ").append(item.getTitle());
                if (item.getOwner() != null) {
                    sb.append(" (Owner: ").append(item.getOwner().getName()).append(")");
                }
                if (item.getDueDate() != null) {
                    sb.append(" [Due: ").append(item.getDueDate()).append("]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}

