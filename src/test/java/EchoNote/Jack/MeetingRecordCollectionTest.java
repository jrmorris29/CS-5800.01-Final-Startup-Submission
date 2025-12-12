package EchoNote.Jack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for MeetingRecordCollection: Iterator pattern aggregate. */
public class MeetingRecordCollectionTest {

    private MeetingRecord meeting1;
    private MeetingRecord meeting2;
    private MeetingRecord meeting3;

    @BeforeEach
    void setUp() {
        meeting1 = new MeetingRecordBuilder()
                .withTitle("Team Standup")
                .withTags(Arrays.asList("daily", "standup"))
                .build();
        meeting1.setStatus(ApprovalStatus.APPROVED);

        meeting2 = new MeetingRecordBuilder()
                .withTitle("Project Review")
                .withTags(Arrays.asList("review", "quarterly"))
                .build();
        meeting2.setStatus(ApprovalStatus.DRAFT);

        meeting3 = new MeetingRecordBuilder()
                .withTitle("Daily Standup Notes")
                .withTags(Arrays.asList("daily", "notes"))
                .build();
        meeting3.setStatus(ApprovalStatus.APPROVED);
    }

    @Nested
    @DisplayName("Iterable Interface Tests")
    class IterableInterfaceTests {

        @Test
        @DisplayName("Collection works with for-each loop")
        void collection_worksWithForEachLoop() {
            MeetingRecordCollection collection = new MeetingRecordCollection(
                    Arrays.asList(meeting1, meeting2, meeting3));
            
            List<MeetingRecord> collected = new ArrayList<>();
            for (MeetingRecord record : collection) {
                collected.add(record);
            }
            
            assertEquals(3, collected.size());
            assertTrue(collected.contains(meeting1));
            assertTrue(collected.contains(meeting2));
            assertTrue(collected.contains(meeting3));
        }

        @Test
        @DisplayName("Multiple iterators are independent")
        void multipleIterators_areIndependent() {
            MeetingRecordCollection collection = new MeetingRecordCollection(
                    Arrays.asList(meeting1, meeting2));
            
            Iterator<MeetingRecord> iter1 = collection.iterator();
            Iterator<MeetingRecord> iter2 = collection.iterator();
            
            // Advance iter1
            iter1.next();
            
            // iter2 should still be at the beginning
            assertSame(meeting1, iter2.next());
        }
    }

    @Nested
    @DisplayName("Specialized Iterator Factory Methods")
    class SpecializedIteratorTests {

        @Test
        @DisplayName("approvedIterator returns only approved records")
        void approvedIterator_returnsOnlyApproved() {
            MeetingRecordCollection collection = new MeetingRecordCollection(
                    Arrays.asList(meeting1, meeting2, meeting3));
            
            Iterator<MeetingRecord> approved = collection.approvedIterator();
            
            List<MeetingRecord> result = new ArrayList<>();
            while (approved.hasNext()) {
                result.add(approved.next());
            }
            
            assertEquals(2, result.size());
            assertTrue(result.contains(meeting1));
            assertTrue(result.contains(meeting3));
            assertFalse(result.contains(meeting2)); // meeting2 is DRAFT
        }

        @Test
        @DisplayName("iteratorByTag filters by tag")
        void iteratorByTag_filtersByTag() {
            MeetingRecordCollection collection = new MeetingRecordCollection(
                    Arrays.asList(meeting1, meeting2, meeting3));
            
            Iterator<MeetingRecord> dailyIterator = collection.iteratorByTag("daily");
            
            List<MeetingRecord> result = new ArrayList<>();
            while (dailyIterator.hasNext()) {
                result.add(dailyIterator.next());
            }
            
            assertEquals(2, result.size());
            assertTrue(result.contains(meeting1));
            assertTrue(result.contains(meeting3));
        }

        @Test
        @DisplayName("iteratorByTitle filters by title substring")
        void iteratorByTitle_filtersByTitleSubstring() {
            MeetingRecordCollection collection = new MeetingRecordCollection(
                    Arrays.asList(meeting1, meeting2, meeting3));
            
            Iterator<MeetingRecord> standupIterator = collection.iteratorByTitle("Standup");
            
            List<MeetingRecord> result = new ArrayList<>();
            while (standupIterator.hasNext()) {
                result.add(standupIterator.next());
            }
            
            assertEquals(2, result.size());
            assertTrue(result.contains(meeting1));  // "Team Standup"
            assertTrue(result.contains(meeting3));  // "Daily Standup Notes"
        }

        @Test
        @DisplayName("iteratorWithFilter uses custom predicate")
        void iteratorWithFilter_usesCustomPredicate() {
            MeetingRecordCollection collection = new MeetingRecordCollection(
                    Arrays.asList(meeting1, meeting2, meeting3));
            
            // Custom filter: records with "review" in tags
            Iterator<MeetingRecord> customIterator = collection.iteratorWithFilter(
                    record -> record.getTags().contains("review"));
            
            assertTrue(customIterator.hasNext());
            assertSame(meeting2, customIterator.next());
            assertFalse(customIterator.hasNext());
        }
    }
}
