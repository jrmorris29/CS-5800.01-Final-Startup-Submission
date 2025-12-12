package EchoNote.Jack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for MeetingRecordIterator: Iterator pattern. */
public class MeetingRecordIteratorTest {

    private List<MeetingRecord> testRecords;
    private MeetingRecord draftMeeting;
    private MeetingRecord approvedMeeting;
    private MeetingRecord urgentMeeting;

    @BeforeEach
    void setUp() {
        draftMeeting = new MeetingRecordBuilder()
                .withTitle("Draft Planning Session")
                .withTags(Arrays.asList("planning", "draft"))
                .build();
        draftMeeting.setStatus(ApprovalStatus.DRAFT);

        approvedMeeting = new MeetingRecordBuilder()
                .withTitle("Approved Budget Review")
                .withTags(Arrays.asList("budget", "approved"))
                .build();
        approvedMeeting.setStatus(ApprovalStatus.APPROVED);

        urgentMeeting = new MeetingRecordBuilder()
                .withTitle("Urgent Security Meeting")
                .withTags(Arrays.asList("urgent", "security"))
                .build();
        urgentMeeting.setStatus(ApprovalStatus.APPROVED);

        testRecords = Arrays.asList(draftMeeting, approvedMeeting, urgentMeeting);
    }

    @Nested
    @DisplayName("Basic Iterator Functionality")
    class BasicIteratorTests {

        @Test
        @DisplayName("Iterator traverses all elements in sequence")
        void iterator_traversesAllElements() {
            MeetingRecordIterator iterator = new MeetingRecordIterator(testRecords);
            
            List<MeetingRecord> collected = new ArrayList<>();
            while (iterator.hasNext()) {
                collected.add(iterator.next());
            }
            
            assertEquals(3, collected.size());
            assertSame(draftMeeting, collected.get(0));
            assertSame(approvedMeeting, collected.get(1));
            assertSame(urgentMeeting, collected.get(2));
        }

        @Test
        @DisplayName("hasNext returns false for empty collection")
        void hasNext_returnsFalse_forEmptyCollection() {
            MeetingRecordIterator iterator = new MeetingRecordIterator(new ArrayList<>());
            
            assertFalse(iterator.hasNext());
        }

        @Test
        @DisplayName("next throws NoSuchElementException when exhausted")
        void next_throwsException_whenExhausted() {
            MeetingRecordIterator iterator = new MeetingRecordIterator(Arrays.asList(draftMeeting));
            
            iterator.next(); // consume the only element
            
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        @DisplayName("Multiple hasNext calls don't advance iterator")
        void multipleHasNextCalls_dontAdvanceIterator() {
            MeetingRecordIterator iterator = new MeetingRecordIterator(testRecords);
            
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            
            // First next() should still return first element
            assertSame(draftMeeting, iterator.next());
        }
    }

    @Nested
    @DisplayName("Filtered Iterator Tests")
    class FilteredIteratorTests {

        @Test
        @DisplayName("approvedOnly iterator returns only approved records")
        void approvedOnly_returnsOnlyApprovedRecords() {
            Iterator<MeetingRecord> iterator = MeetingRecordIterator.approvedOnly(testRecords);
            
            List<MeetingRecord> approved = new ArrayList<>();
            while (iterator.hasNext()) {
                approved.add(iterator.next());
            }
            
            assertEquals(2, approved.size());
            assertTrue(approved.contains(approvedMeeting));
            assertTrue(approved.contains(urgentMeeting));
            assertFalse(approved.contains(draftMeeting));
        }

        @Test
        @DisplayName("byTag iterator returns records with matching tag")
        void byTag_returnsRecordsWithMatchingTag() {
            Iterator<MeetingRecord> iterator = MeetingRecordIterator.byTag(testRecords, "urgent");
            
            assertTrue(iterator.hasNext());
            assertSame(urgentMeeting, iterator.next());
            assertFalse(iterator.hasNext());
        }

        @Test
        @DisplayName("byTitleContaining iterator filters by title")
        void byTitleContaining_filtersByTitle() {
            Iterator<MeetingRecord> iterator = 
                    MeetingRecordIterator.byTitleContaining(testRecords, "Budget");
            
            assertTrue(iterator.hasNext());
            assertSame(approvedMeeting, iterator.next());
            assertFalse(iterator.hasNext());
        }
    }
}
