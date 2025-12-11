package EchoNote.Arpit;

import EchoNote.Jack.ExportFormat;
import EchoNote.Jack.ExportResult;
import EchoNote.Jack.MeetingRecord;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/**
 * Export service using Strategy pattern for multi-format export support.
 * Delegates to specific MeetingExporter implementations based on format.
 */
public class ExportService {

    private final File exportDirectory;
    private final Map<ExportFormat, MeetingExporter> exporters;

    public ExportService() {
        this(new File("exports"));
    }

    public ExportService(File exportDirectory) {
        this.exportDirectory = exportDirectory;
        this.exporters = new EnumMap<>(ExportFormat.class);

        // Register built-in exporters
        exporters.put(ExportFormat.MARKDOWN, new MarkdownExporter(exportDirectory));
        exporters.put(ExportFormat.HTML, new HtmlExporter(exportDirectory));
    }

    /**
     * Exports a meeting record using the specified format.
     * Uses Strategy pattern to delegate to the appropriate exporter.
     *
     * @param record the meeting record to export
     * @param format the export format
     * @return the export result
     * @throws UnsupportedExportException if the format is not supported
     */
    public ExportResult export(MeetingRecord record, ExportFormat format) {
        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");
        }
        if (format == null) {
            throw new IllegalArgumentException("format cannot be null");
        }

        MeetingExporter exporter = exporters.get(format);
        if (exporter == null) {
            throw new UnsupportedExportException("Export format not supported: " + format);
        }

        return exporter.export(record);
    }

    /**
     * Exports a meeting record as Markdown (convenience method).
     */
    public ExportResult exportAsMarkdown(MeetingRecord record) {
        return export(record, ExportFormat.MARKDOWN);
    }

    /**
     * Exports a meeting record as HTML (convenience method).
     */
    public ExportResult exportAsHtml(MeetingRecord record) {
        return export(record, ExportFormat.HTML);
    }

    /**
     * Returns the list of supported export formats.
     */
    public ExportFormat[] getSupportedFormats() {
        return exporters.keySet().toArray(new ExportFormat[0]);
    }

    public File getExportDirectory() {
        return exportDirectory;
    }
}
