package EchoNote.Jack;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceTest {

    private MeetingRecord createRecord(String title, String... tags) {
        MeetingRecordBuilder builder = new MeetingRecordBuilder()
                .withTitle(title)
                .withDate(LocalDateTime.now());

        if (tags != null && tags.length > 0) {
            builder.withTags(Arrays.asList(tags));
        }

        return builder.build();
    }

    @Test
    void saveAndGetById_withUuid_returnsSameInstance() {
        Workspace workspace = new Workspace();
        MeetingRecord record = createRecord("Weekly Sync", "team", "status");

        workspace.save(record);

        MeetingRecord found = workspace.getById(record.getId());
        assertSame(record, found,
                "Workspace should return the same MeetingRecord instance that was saved");
    }

    @Test
    void saveAndGetById_withStringId_returnsSameInstance() {
        Workspace workspace = new Workspace();
        MeetingRecord record = createRecord("Planning Meeting", "planning");

        workspace.save(record);

        String idString = record.getId().toString();
        MeetingRecord found = workspace.getById(idString);

        assertSame(record, found,
                "Workspace should find MeetingRecord by its String id");
    }

    @Test
    void savingSameRecordTwice_replacesExistingRecordWithoutDuplicates() {
        Workspace workspace = new Workspace();
        MeetingRecord record = createRecord("Design Review", "design");

        workspace.save(record);
        workspace.save(record);

        List<MeetingRecord> all = workspace.getAll();
        assertEquals(1, all.size(),
                "Saving the same record twice should not create duplicates");
        assertSame(record, all.get(0));
    }

    @Test
    void findByQuery_matchesTitleAndTags() {
        Workspace workspace = new Workspace();

        MeetingRecord kickoff = createRecord("EchoNote Kickoff", "client", "alpha");
        MeetingRecord sync = createRecord("Weekly Status Sync", "status", "project-x");
        MeetingRecord social = createRecord("Coffee Chat", "social");

        workspace.save(kickoff);
        workspace.save(sync);
        workspace.save(social);

        List<MeetingRecord> byTitle = workspace.findByQuery("kickoff");
        assertEquals(1, byTitle.size(), "Should find only the kickoff meeting by title");
        assertSame(kickoff, byTitle.get(0));

        List<MeetingRecord> byTag = workspace.findByQuery("project");
        assertEquals(1, byTag.size(), "Should find meetings whose tags contain the query substring");
        assertSame(sync, byTag.get(0));

        List<MeetingRecord> allWhenBlank = workspace.findByQuery("   ");
        assertEquals(3, allWhenBlank.size(),
                "Blank query should return all stored records");
    }

    @Test
    void getById_unknownUuid_throwsRecordNotFoundException() {
        Workspace workspace = new Workspace();
        UUID randomId = UUID.randomUUID();

        assertThrows(RecordNotFoundException.class,
                () -> workspace.getById(randomId),
                "Unknown UUID should trigger RecordNotFoundException");
    }

    @Test
    void getById_blankString_throwsIllegalArgumentException() {
        Workspace workspace = new Workspace();

        assertThrows(IllegalArgumentException.class,
                () -> workspace.getById("  "),
                "Blank id string should throw IllegalArgumentException");
    }

    @Test
    void clearAll_removesAllRecords() {
        Workspace workspace = new Workspace();
        workspace.save(createRecord("Meeting 1"));
        workspace.save(createRecord("Meeting 2"));
        workspace.save(createRecord("Meeting 3"));

        assertEquals(3, workspace.getAll().size(), "Should have 3 records before clearing");

        boolean result = workspace.clearAll();

        assertTrue(result, "clearAll should return true on success");
        assertEquals(0, workspace.getAll().size(), "Should have 0 records after clearing");
    }

    @Test
    void clearAll_emptyWorkspace_returnsTrue() {
        Workspace workspace = new Workspace();

        boolean result = workspace.clearAll();

        assertTrue(result, "clearAll on empty workspace should return true");
        assertEquals(0, workspace.getAll().size());
    }
}
