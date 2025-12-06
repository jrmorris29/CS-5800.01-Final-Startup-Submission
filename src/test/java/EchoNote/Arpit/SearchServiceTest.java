package EchoNote.Arpit;

import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.MeetingRecordBuilder;
import EchoNote.Jack.Workspace;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchServiceTest {

    private MeetingRecord createRecord(String title) {
        return new MeetingRecordBuilder()
                .withTitle(title)
                .withDate(LocalDateTime.now())
                .build();
    }

    @Test
    void index_nullRecord_throwsIllegalArgumentException() {
        Workspace workspace = new Workspace();
        SearchService service = new SearchService(workspace);

        assertThrows(IllegalArgumentException.class,
                () -> service.index(null),
                "Indexing a null record should throw IllegalArgumentException");
    }

    @Test
    void search_blankOrNullQuery_returnsAllIndexedRecords() {
        Workspace workspace = new Workspace();
        SearchService service = new SearchService(workspace);

        MeetingRecord r1 = createRecord("AI Planning");
        MeetingRecord r2 = createRecord("Budget Review");
        service.index(r1);
        service.index(r2);

        List<MeetingRecord> allForNull = service.search(null);
        List<MeetingRecord> allForBlank = service.search("  ");

        assertEquals(2, allForNull.size(), "Null query should return all indexed records");
        assertTrue(allForNull.contains(r1) && allForNull.contains(r2));

        assertEquals(2, allForBlank.size(), "Blank query should return all indexed records");
        assertTrue(allForBlank.contains(r1) && allForBlank.contains(r2));
    }

    @Test
    void search_matchesOnTitleCaseInsensitively() {
        Workspace workspace = new Workspace();
        SearchService service = new SearchService(workspace);

        MeetingRecord aiMeeting = createRecord("AI Strategy Meeting");
        MeetingRecord financeMeeting = createRecord("Finance Review");

        service.index(aiMeeting);
        service.index(financeMeeting);

        List<MeetingRecord> results = service.search("strategy");

        assertEquals(1, results.size(), "Only the AI Strategy meeting should match 'strategy'");
        assertSame(aiMeeting, results.get(0));
    }
}
