package EchoNote.Jack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Workspace {

    private final List<MeetingRecord> records = new ArrayList<>();

    public synchronized void save(MeetingRecord record) {
        Objects.requireNonNull(record, "record cannot be null");

        records.removeIf(r -> r.getId().equals(record.getId()));
        records.add(record);
    }

    public synchronized List<MeetingRecord> findByQuery(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(records);
        }

        String lower = query.toLowerCase();

        return records.stream()
                .filter(rec ->
                        (rec.getTitle() != null &&
                                rec.getTitle().toLowerCase().contains(lower)) ||
                                rec.getTags().stream()
                                        .anyMatch(t -> t.toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }

    public synchronized MeetingRecord getById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        return records.stream()
                .filter(rec -> rec.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new RecordNotFoundException("No MeetingRecord found with id " + id));
    }

    public synchronized MeetingRecord getById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }
        return getById(UUID.fromString(id.trim()));
    }

    public synchronized List<MeetingRecord> getAll() {
        return new ArrayList<>(records);
    }
}
