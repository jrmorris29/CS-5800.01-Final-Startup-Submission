package EchoNote.Arpit;

import EchoNote.Jack.ExportResult;
import EchoNote.Jack.MeetingRecord;

/**
 * Interface for exporting meeting records to various formats.
 */
public interface MeetingExporter {

    /**
     * Exports a meeting record to a specific format.
     *
     * @param record the meeting record to export
     * @return the result of the export operation
     */
    ExportResult export(MeetingRecord record);

    /**
     * Returns the file extension for this exporter (e.g., "md", "html").
     *
     * @return the file extension without the leading dot
     */
    String getFileExtension();
}

