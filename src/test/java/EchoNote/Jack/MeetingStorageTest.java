package EchoNote.Jack;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MeetingStorageTest {

    private File tempFile;
    private MeetingStorage storage;

    @BeforeEach
    void setUp() throws IOException {
        Path tempDir = Files.createTempDirectory("echonote-storage-test");
        tempFile = new File(tempDir.toFile(), "test-meetings.json");
        storage = new MeetingStorage(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    private MeetingRecord createTestRecord(String title) {
        Participant owner = new Participant("Test User", "test@example.com", "Lead");
        Summary summary = new Summary("sum-1");
        summary.setNotes("Test summary notes.");
        summary.addTopic("Topic 1");
        summary.addDecision("Decision 1");

        ActionItem action = new ActionItem("act-1", "Test action", owner, LocalDate.now().plusDays(7));

        return new MeetingRecordBuilder()
                .withTitle(title)
                .withDate(LocalDateTime.of(2025, 1, 15, 10, 0))
                .withParticipants(List.of(owner))
                .withSummary(summary)
                .withActions(List.of(action))
                .build();
    }

    @Test
    void save_and_load_roundTrip_preservesData() {
        MeetingRecord original = createTestRecord("Persistence Test Meeting");

        storage.save(List.of(original));
        List<MeetingRecord> loaded = storage.load();

        assertEquals(1, loaded.size(), "Should load exactly 1 record");
        MeetingRecord loadedRecord = loaded.get(0);

        assertEquals(original.getId(), loadedRecord.getId(), "ID should be preserved");
        assertEquals(original.getTitle(), loadedRecord.getTitle(), "Title should be preserved");
        assertEquals(original.getDate(), loadedRecord.getDate(), "Date should be preserved");
        assertNotNull(loadedRecord.getSummary(), "Summary should be preserved");
        assertEquals(original.getSummary().getNotes(), loadedRecord.getSummary().getNotes());
        assertEquals(1, loadedRecord.getActions().size(), "Actions should be preserved");
        assertEquals(1, loadedRecord.getParticipants().size(), "Participants should be preserved");
    }

    @Test
    void load_nonExistentFile_returnsEmptyList() {
        File nonExistent = new File("non-existent-file-12345.json");
        MeetingStorage emptyStorage = new MeetingStorage(nonExistent);

        List<MeetingRecord> loaded = emptyStorage.load();

        assertNotNull(loaded);
        assertTrue(loaded.isEmpty(), "Should return empty list for non-existent file");
    }

    @Test
    void save_emptyList_createsEmptyFile() {
        storage.save(List.of());

        assertTrue(tempFile.exists(), "File should exist after saving empty list");
        List<MeetingRecord> loaded = storage.load();
        assertTrue(loaded.isEmpty());
    }

    @Test
    void save_multipleRecords_allPreserved() {
        MeetingRecord rec1 = createTestRecord("Meeting 1");
        MeetingRecord rec2 = createTestRecord("Meeting 2");
        MeetingRecord rec3 = createTestRecord("Meeting 3");

        storage.save(List.of(rec1, rec2, rec3));
        List<MeetingRecord> loaded = storage.load();

        assertEquals(3, loaded.size(), "All 3 records should be loaded");
    }

    @Test
    void clearHistory_removesFile() {
        storage.save(List.of(createTestRecord("To be deleted")));
        assertTrue(tempFile.exists());

        boolean cleared = storage.clearHistory();

        assertTrue(cleared, "clearHistory should return true");
        assertFalse(tempFile.exists(), "File should be deleted after clearHistory");
    }

    @Test
    void hasStoredData_returnsTrueWhenFileExists() {
        assertFalse(storage.hasStoredData(), "Should return false before saving");

        storage.save(List.of(createTestRecord("Test")));

        assertTrue(storage.hasStoredData(), "Should return true after saving data");
    }
}
