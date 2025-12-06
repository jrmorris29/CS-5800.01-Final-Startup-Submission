package EchoNote.Arpit;

import EchoNote.Jack.ActionItem;
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
}
