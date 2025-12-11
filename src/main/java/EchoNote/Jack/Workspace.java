package EchoNote.Jack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Workspace {

    private final List<MeetingRecord> records = new ArrayList<>();
    private MeetingStorage storage;

    public Workspace() {
        // Default constructor without persistence
    }

    /**
     * Creates a Workspace with persistence support.
     * Automatically loads existing records from storage.
     *
     * @param storage the storage to use for persistence
     */
    public Workspace(MeetingStorage storage) {
        this.storage = storage;
        loadFromStorage();
    }

    /**
     * Loads records from storage. Called on initialization.
     */
    private void loadFromStorage() {
        if (storage != null) {
            try {
                List<MeetingRecord> loaded = storage.load();
                records.addAll(loaded);
                System.out.println("[Workspace] Loaded " + loaded.size() + " meetings from storage.");
            } catch (StorageException e) {
                System.err.println("[Workspace] Warning: Failed to load from storage: " + e.getMessage());
            }
        }
    }

    /**
     * Persists current records to storage.
     */
    private void persistToStorage() {
        if (storage != null) {
            try {
                storage.save(records);
            } catch (StorageException e) {
                System.err.println("[Workspace] Warning: Failed to persist to storage: " + e.getMessage());
            }
        }
    }

    public synchronized void save(MeetingRecord record) {
        Objects.requireNonNull(record, "record cannot be null");

        records.removeIf(r -> r.getId().equals(record.getId()));
        records.add(record);
        persistToStorage();
    }

    /**
     * Clears all meeting history from memory and storage.
     *
     * @return true if cleared successfully
     */
    public synchronized boolean clearAll() {
        records.clear();
        if (storage != null) {
            return storage.clearHistory();
        }
        return true;
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
