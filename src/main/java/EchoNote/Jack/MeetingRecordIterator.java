package EchoNote.Jack;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Iterator Pattern: Sequential access to MeetingRecord elements with optional filtering.
 */
public class MeetingRecordIterator implements Iterator<MeetingRecord> {
    
    private final List<MeetingRecord> records;
    private final Predicate<MeetingRecord> filter;
    private int currentIndex;
    private MeetingRecord nextRecord;
    private boolean hasNextComputed;

    /** Creates an iterator over all records. */
    public MeetingRecordIterator(List<MeetingRecord> records) {
        this(records, null);
    }

    /** Creates a filtered iterator. */
    public MeetingRecordIterator(List<MeetingRecord> records, Predicate<MeetingRecord> filter) {
        this.records = records != null ? records : List.of();
        this.filter = filter;
        this.currentIndex = 0;
        this.hasNextComputed = false;
        this.nextRecord = null;
    }

    @Override
    public boolean hasNext() {
        if (!hasNextComputed) {
            computeNext();
        }
        return nextRecord != null;
    }

    @Override
    public MeetingRecord next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more meeting records");
        }
        MeetingRecord result = nextRecord;
        hasNextComputed = false;
        nextRecord = null;
        return result;
    }


    private void computeNext() {
        nextRecord = null;
        while (currentIndex < records.size()) {
            MeetingRecord candidate = records.get(currentIndex);
            currentIndex++;
            
            if (filter == null || filter.test(candidate)) {
                nextRecord = candidate;
                break;
            }
        }
        hasNextComputed = true;
    }

    /** Factory: iterator for approved records only. */
    public static MeetingRecordIterator approvedOnly(List<MeetingRecord> records) {
        return new MeetingRecordIterator(records, MeetingRecord::isApproved);
    }

    /** Factory: iterator for records with a specific tag. */
    public static MeetingRecordIterator byTag(List<MeetingRecord> records, String tag) {
        return new MeetingRecordIterator(records, 
                record -> record.getTags().stream()
                        .anyMatch(t -> t.equalsIgnoreCase(tag)));
    }

    /** Factory: iterator for records with title containing search term. */
    public static MeetingRecordIterator byTitleContaining(List<MeetingRecord> records, String searchTerm) {
        String lowerSearch = searchTerm != null ? searchTerm.toLowerCase() : "";
        return new MeetingRecordIterator(records,
                record -> record.getTitle() != null && 
                        record.getTitle().toLowerCase().contains(lowerSearch));
    }
}

