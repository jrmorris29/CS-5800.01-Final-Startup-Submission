package EchoNote.Arpit;

import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.Workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SearchService {

    private final Workspace workspace;
    private final List<MeetingRecord> indexedRecords = new ArrayList<>();

    public SearchService(Workspace workspace) {
        this.workspace = workspace;
        // Index any existing records from workspace (e.g., loaded from storage)
        indexExistingRecords();
    }

    /**
     * Indexes all existing records from the workspace.
     * Called on initialization to support loading from storage.
     */
    private void indexExistingRecords() {
        for (MeetingRecord record : workspace.getAll()) {
            index(record);
        }
    }

    public void index(MeetingRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("record must not be null");
        }
        if (!indexedRecords.contains(record)) {
            indexedRecords.add(record);
        }
    }

    /**
     * Clears all indexed records.
     */
    public void clearIndex() {
        indexedRecords.clear();
    }

    public List<MeetingRecord> search(String query) {
        if (query == null || query.isBlank()) {
            return Collections.unmodifiableList(new ArrayList<>(indexedRecords));
        }

        String normalized = query.toLowerCase();
        List<MeetingRecord> results = new ArrayList<>();

        for (MeetingRecord record : indexedRecords) {
            if (record == null) {
                continue;
            }

            StringBuilder searchable = new StringBuilder();

            if (record.getTitle() != null) {
                searchable.append(record.getTitle()).append(" ");
            }
            if (record.getSummary() != null) {
                searchable.append(record.getSummary().toString()).append(" ");
            }
            if (record.getTranscript() != null) {
                searchable.append(record.getTranscript().toString()).append(" ");
            }
            if (record.getActions() != null) {
                record.getActions().forEach(ai -> {
                    if (ai != null) {
                        searchable.append(ai.toString()).append(" ");
                    }
                });
            }

            if (searchable.toString().toLowerCase().contains(normalized)) {
                results.add(record);
            }
        }

        return results;
    }
}
