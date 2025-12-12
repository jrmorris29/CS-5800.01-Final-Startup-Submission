package EchoNote.Arpit;

import EchoNote.Jack.ExportResult;
import EchoNote.Jack.MeetingFormatter;
import EchoNote.Jack.MeetingRecord;

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

        sb.append("# Meeting ").append(MeetingFormatter.getTitleOrId(record)).append("\n\n");

        if (record.getDate() != null) {
            sb.append("Date: ").append(record.getDate()).append("\n\n");
        }

        if (record.getSummary() != null) {
            sb.append("## Summary\n\n");
            sb.append(MeetingFormatter.formatSummaryAsText(record.getSummary()));
        }

        if (!record.getActions().isEmpty()) {
            sb.append("## Action Items\n");
            sb.append(MeetingFormatter.formatActionsAsText(record.getActions()));
        }

        return sb.toString();
    }
}

