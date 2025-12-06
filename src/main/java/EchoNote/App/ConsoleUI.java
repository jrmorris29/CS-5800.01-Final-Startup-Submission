package EchoNote.App;

import EchoNote.Arpit.EmailNotifier;
import EchoNote.Arpit.ExportService;
import EchoNote.Arpit.SearchService;
import EchoNote.Jack.ActionItem;
import EchoNote.Jack.ExportResult;
import EchoNote.Jack.MeetingRecord;
import EchoNote.Jack.RecordNotFoundException;
import EchoNote.Jack.Summary;
import EchoNote.Jack.Transcript;
import EchoNote.Jack.Workspace;
import EchoNote.Mihail.Recorder;
import EchoNote.Mihail.Summarizer;
import EchoNote.Mihail.Transcriber;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleUI {

    private final Workspace workspace;
    private final Transcriber transcriber;
    private final Summarizer summarizer;
    private final ExportService exportService;
    private final SearchService searchService;
    private final EmailNotifier emailNotifier;
    private final Recorder recorder;

    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(AppConfig config) {
        this.workspace = config.getWorkspace();
        this.transcriber = config.getTranscriber();
        this.summarizer = config.getSummarizer();
        this.exportService = config.getExportService();
        this.searchService = config.getSearchService();
        this.emailNotifier = config.getEmailNotifier();
        this.recorder = new Recorder();
    }

    public void run() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleNewMeetingFromAudio();
                case "2" -> handleSearchMeetings();
                case "3" -> handleExportMeeting();
                case "4" -> handleEmailSummary();
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Unknown option. Please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== EchoNote Demo ===");
        System.out.println("1) New meeting from WAV / mic");
        System.out.println("2) Search / list meetings");
        System.out.println("3) Export meeting as Markdown");
        System.out.println("4) Email meeting summary");
        System.out.println("0) Exit");
        System.out.print("Choose option: ");
    }

    private void handleNewMeetingFromAudio() {
        try {
            File wavFile = chooseAudioSource();
            if (wavFile == null) {
                return;
            }

            System.out.println("Transcribing audio...");
            Transcript transcript = transcriber.transcribeFile(wavFile.toPath());

            System.out.println("Generating summary...");
            Summary summary = summarizer.summarize(transcript);

            System.out.println("Extracting action items...");
            List<ActionItem> actions = summarizer.extractActions(transcript);

            MeetingRecord record = buildMeetingRecord(transcript, summary, actions);
            workspace.save(record);

            System.out.println("Meeting saved with ID: " + record.getId());
        } catch (Exception e) {
            System.out.println("Error while creating meeting: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    private File chooseAudioSource() {
        System.out.println();
        System.out.println("Choose audio source:");
        System.out.println("1) Existing WAV file");
        System.out.println("2) Record from microphone");
        System.out.print("Enter choice (1 or 2): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> {
                System.out.print("Enter path to WAV file: ");
                String path = scanner.nextLine().trim();
                File wavFile = new File(path);

                if (!wavFile.exists() || !wavFile.isFile()) {
                    System.out.println("File not found: " + wavFile.getAbsolutePath());
                    return null;
                }
                return wavFile;
            }
            case "2" -> {
                try {
                    System.out.print("Enter recording duration in seconds: ");
                    String secondsInput = scanner.nextLine().trim();
                    int seconds = Integer.parseInt(secondsInput);

                    Path recordedPath = recorder.recordToTempFile(
                            "echonote-recording-",
                            Duration.ofSeconds(seconds)
                    );
                    System.out.println("Recording finished. Saved to: " + recordedPath);
                    return recordedPath.toFile();
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid number for seconds.");
                    return null;
                } catch (RuntimeException ex) {
                    System.out.println("Recording failed: " + ex.getMessage());
                    return null;
                }
            }
            default -> {
                System.out.println("Invalid choice.");
                return null;
            }
        }
    }


    private MeetingRecord buildMeetingRecord(Transcript transcript,
                                             Summary summary,
                                             List<ActionItem> actions) {
        MeetingRecord record = new MeetingRecord();
        record.setTitle("Demo Meeting");
        record.setDate(LocalDateTime.now());
        record.setTranscript(transcript);
        record.setSummary(summary);
        record.setActions(actions);
        return record;
    }

    private void handleSearchMeetings() {
        System.out.print("Enter search query (blank for all): ");
        String query = scanner.nextLine().trim();

        List<MeetingRecord> results = searchService.search(query);
        if (results.isEmpty()) {
            System.out.println("No meetings found.");
            return;
        }

        System.out.println("Found " + results.size() + " meeting(s):");
        for (MeetingRecord record : results) {
            System.out.println("- ID: " + record.getId()
                    + " | Title: " + record.getTitle()
                    + " | Date: " + record.getDate());
        }
    }

    private void handleExportMeeting() {
        List<MeetingRecord> all = searchService.search("");
        if (all.isEmpty()) {
            System.out.println("No meetings available to export.");
            return;
        }

        System.out.println("Available meetings:");
        for (MeetingRecord record : all) {
            System.out.println("- ID: " + record.getId()
                    + " | Title: " + record.getTitle()
                    + " | Date: " + record.getDate());
        }

        System.out.print("Enter meeting ID to export: ");
        String idInput = scanner.nextLine().trim();

        MeetingRecord record = findRecordById(idInput);
        if (record == null) {
            return;
        }

        ExportResult result = exportService.exportAsMarkdown(record);
        System.out.println("Export success: " + result.isSuccess());
        if (result.getLink() != null) {
            System.out.println("Exported file/link: " + result.getLink());
        }
        if (result.getMessage() != null) {
            System.out.println(result.getMessage());
        }
    }

    private void handleEmailSummary() {
        System.out.print("Enter meeting ID to email: ");
        String idInput = scanner.nextLine().trim();

        MeetingRecord record = findRecordById(idInput);
        if (record == null) {
            return;
        }

        try {
            emailNotifier.emailParticipants(record, "demo-event");
            System.out.println("Emails sent.");
        } catch (Exception e) {
            System.out.println("Failed to send emails: " + e.getMessage());
        }
    }

    private MeetingRecord findRecordById(String idInput) {
        if (idInput == null || idInput.isBlank()) {
            System.out.println("ID cannot be blank.");
            return null;
        }

        try {
            UUID id = UUID.fromString(idInput.trim());
            return workspace.getById(id);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format.");
            return null;
        } catch (RecordNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
