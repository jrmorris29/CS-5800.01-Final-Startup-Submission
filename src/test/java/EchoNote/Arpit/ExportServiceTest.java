package EchoNote.Arpit;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.ExportFormat;
import EchoNote.Jack.ExportResult;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.MeetingRecordBuilder;
import EchoNote.Jack.Participant;
import EchoNote.Jack.Summary;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ExportServiceTest {

    private MeetingRecord createRichRecord() {
        Participant owner = new Participant("Jack", "jack@example.com", "Lead");
        Summary summary = new Summary("sum-1");
        summary.setNotes("Short summary of the meeting.");
        summary.addTopic("Topic A");
        summary.addDecision("Decision X");

        ActionItem action = new ActionItem("act-1",
                "Follow up with client",
                owner,
                LocalDate.now().plusDays(3));

        return new MeetingRecordBuilder()
                .withTitle("Export Test Meeting")
                .withDate(LocalDateTime.of(2025, 1, 20, 10, 0))
                .withSummary(summary)
                .withActions(java.util.List.of(action))
                .build();
    }

    @Test
    void exportAsMarkdown_createsFileAndReturnsSuccess() throws Exception {
        MeetingRecord record = createRichRecord();

        Path tempDir = Files.createTempDirectory("echonote-export-test");
        File exportDir = tempDir.toFile();

        ExportService service = new ExportService(exportDir);
        ExportResult result = service.exportAsMarkdown(record);

        assertTrue(result.isSuccess(), "ExportResult should indicate success");
        assertNotNull(result.getLink(), "ExportResult should contain a link/path to the file");

        Path exportedPath = Path.of(result.getLink());
        assertTrue(Files.exists(exportedPath), "Exported markdown file should exist on disk");

        String content = Files.readString(exportedPath);
        assertTrue(content.contains("# Meeting Export Test Meeting"),
                "Markdown should contain a heading with the meeting title");
        assertTrue(content.contains("## Summary"),
                "Markdown should contain a Summary section when a summary is present");
        assertTrue(content.contains("Short summary of the meeting."),
                "Markdown should include the summary notes");
    }

    @Test
    void exportAsMarkdown_nullRecord_throwsIllegalArgumentException() {
        ExportService service = new ExportService(new File("exports"));

        assertThrows(IllegalArgumentException.class,
                () -> service.exportAsMarkdown(null),
                "Passing a null record to exportAsMarkdown should throw IllegalArgumentException");
    }

    @Test
    void exportAsHtml_createsFileAndReturnsSuccess() throws Exception {
        MeetingRecord record = createRichRecord();

        Path tempDir = Files.createTempDirectory("echonote-export-test-html");
        File exportDir = tempDir.toFile();

        ExportService service = new ExportService(exportDir);
        ExportResult result = service.exportAsHtml(record);

        assertTrue(result.isSuccess(), "ExportResult should indicate success");
        assertNotNull(result.getLink(), "ExportResult should contain a link/path to the file");

        Path exportedPath = Path.of(result.getLink());
        assertTrue(Files.exists(exportedPath), "Exported HTML file should exist on disk");
        assertTrue(result.getLink().endsWith(".html"), "Exported file should have .html extension");

        String content = Files.readString(exportedPath);
        assertTrue(content.contains("<!DOCTYPE html>"),
                "HTML should contain DOCTYPE declaration");
        assertTrue(content.contains("<title>Meeting: Export Test Meeting</title>"),
                "HTML should contain title tag with meeting title");
        assertTrue(content.contains("Short summary of the meeting."),
                "HTML should include the summary notes");
    }

    @Test
    void export_withExportFormat_delegatesToCorrectExporter() throws Exception {
        MeetingRecord record = createRichRecord();

        Path tempDir = Files.createTempDirectory("echonote-export-test-format");
        File exportDir = tempDir.toFile();

        ExportService service = new ExportService(exportDir);

        ExportResult mdResult = service.export(record, ExportFormat.MARKDOWN);
        assertTrue(mdResult.isSuccess());
        assertTrue(mdResult.getLink().endsWith(".md"));

        ExportResult htmlResult = service.export(record, ExportFormat.HTML);
        assertTrue(htmlResult.isSuccess());
        assertTrue(htmlResult.getLink().endsWith(".html"));
    }

    @Test
    void getSupportedFormats_returnsMarkdownAndHtml() {
        ExportService service = new ExportService(new File("exports"));
        ExportFormat[] formats = service.getSupportedFormats();

        assertEquals(2, formats.length, "Should support 2 formats");
        assertTrue(java.util.Arrays.asList(formats).contains(ExportFormat.MARKDOWN));
        assertTrue(java.util.Arrays.asList(formats).contains(ExportFormat.HTML));
    }

    @Test
    void export_unsupportedFormat_throwsUnsupportedExportException() {
        MeetingRecord record = createRichRecord();
        ExportService service = new ExportService(new File("exports"));

        assertThrows(UnsupportedExportException.class,
                () -> service.export(record, ExportFormat.GOOGLE_DOC),
                "Unsupported format should throw UnsupportedExportException");
    }
}
