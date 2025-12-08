package EchoNote.Jack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Iterator Pattern: Aggregate that creates iterators for MeetingRecord traversal.
 */
public class MeetingRecordCollection implements Iterable<MeetingRecord> {
    
    private final List<MeetingRecord> records;


    public MeetingRecordCollection(List<MeetingRecord> records) {
        this.records = new ArrayList<>(records != null ? records : List.of());
    }


    public MeetingRecordCollection() {
        this(new ArrayList<>());
    }


    public void add(MeetingRecord record) {
        if (record != null) {
            records.add(record);
        }
    }


    public int size() {
        return records.size();
    }


    @Override
    public Iterator<MeetingRecord> iterator() {
        return new MeetingRecordIterator(records);
    }

    /** Returns iterator for approved records only. */
    public Iterator<MeetingRecord> approvedIterator() {
        return MeetingRecordIterator.approvedOnly(records);
    }

    /** Returns iterator filtered by tag. */
    public Iterator<MeetingRecord> iteratorByTag(String tag) {
        return MeetingRecordIterator.byTag(records, tag);
    }

    /** Returns iterator filtered by title. */
    public Iterator<MeetingRecord> iteratorByTitle(String searchTerm) {
        return MeetingRecordIterator.byTitleContaining(records, searchTerm);
    }

    /** Returns iterator with custom filter. */
    public Iterator<MeetingRecord> iteratorWithFilter(Predicate<MeetingRecord> filter) {
        return new MeetingRecordIterator(records, filter);
    }
}

