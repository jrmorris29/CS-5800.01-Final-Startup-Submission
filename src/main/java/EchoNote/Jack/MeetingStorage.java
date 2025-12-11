package EchoNote.Jack;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence of MeetingRecord objects to/from JSON file storage.
 * Provides automatic saving and loading of meeting history.
 */
public class MeetingStorage {

    private static final String DEFAULT_STORAGE_FILE = "echonote-meetings.json";

    private final File storageFile;
    private final ObjectMapper objectMapper;

    public MeetingStorage() {
        this(new File(DEFAULT_STORAGE_FILE));
    }

    public MeetingStorage(File storageFile) {
        this.storageFile = storageFile;
        this.objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Saves a list of meeting records to the storage file.
     *
     * @param records the list of MeetingRecord objects to persist
     * @throws StorageException if saving fails
     */
    public void save(List<MeetingRecord> records) {
        if (records == null) {
            records = new ArrayList<>();
        }

        try {
            List<MeetingRecordDto> dtos = records.stream()
                    .map(MeetingRecordDto::fromMeetingRecord)
                    .toList();
            objectMapper.writeValue(storageFile, dtos);
        } catch (IOException e) {
            throw new StorageException("Failed to save meetings to " + storageFile.getAbsolutePath(), e);
        }
    }

    /**
     * Loads meeting records from the storage file.
     *
     * @return list of MeetingRecord objects, or empty list if file doesn't exist
     * @throws StorageException if loading fails (other than file not existing)
     */
    public List<MeetingRecord> load() {
        if (!storageFile.exists()) {
            return new ArrayList<>();
        }

        try {
            List<MeetingRecordDto> dtos = objectMapper.readValue(
                    storageFile,
                    new TypeReference<List<MeetingRecordDto>>() {}
            );
            return dtos.stream()
                    .map(MeetingRecordDto::toMeetingRecord)
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            throw new StorageException("Failed to load meetings from " + storageFile.getAbsolutePath(), e);
        }
    }

    /**
     * Clears all stored meeting history by deleting the storage file.
     *
     * @return true if the file was deleted or didn't exist, false otherwise
     */
    public boolean clearHistory() {
        if (!storageFile.exists()) {
            return true;
        }
        return storageFile.delete();
    }

    /**
     * Checks if a storage file exists with saved meetings.
     *
     * @return true if storage file exists
     */
    public boolean hasStoredData() {
        return storageFile.exists() && storageFile.length() > 0;
    }

    public File getStorageFile() {
        return storageFile;
    }
}

