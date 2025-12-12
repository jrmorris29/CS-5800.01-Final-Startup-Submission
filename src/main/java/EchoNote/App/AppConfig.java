package EchoNote.App;

import EchoNote.Arpit.ExportService;
import EchoNote.Arpit.SearchService;
import EchoNote.Jack.MeetingStorage;
import EchoNote.Jack.Workspace;
import EchoNote.Mihail.EmailDraftService;
import EchoNote.Mihail.Summarizer;
import EchoNote.Mihail.Transcriber;

public class AppConfig {

    private final Workspace workspace;
    private final MeetingStorage meetingStorage;
    private final Transcriber transcriber;
    private final Summarizer summarizer;
    private final ExportService exportService;
    private final SearchService searchService;
    private final EmailDraftService emailDraftService;

    public AppConfig() {
        // Initialize storage for persistence
        this.meetingStorage = new MeetingStorage();

        // Create workspace with storage support (auto-loads saved meetings)
        this.workspace = new Workspace(meetingStorage);

        this.transcriber = new Transcriber();
        this.summarizer = new Summarizer();

        this.exportService = new ExportService();
        this.searchService = new SearchService(workspace);
        this.emailDraftService = new EmailDraftService();
    }

    public MeetingStorage getMeetingStorage() {
        return meetingStorage;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public Transcriber getTranscriber() {
        return transcriber;
    }

    public Summarizer getSummarizer() {
        return summarizer;
    }

    public ExportService getExportService() {
        return exportService;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public EmailDraftService getEmailDraftService() {
        return emailDraftService;
    }
}
