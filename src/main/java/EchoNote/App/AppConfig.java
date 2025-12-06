package EchoNote.App;

import EchoNote.Arpit.EmailNotifier;
import EchoNote.Arpit.ExportService;
import EchoNote.Arpit.SearchService;
import EchoNote.Jack.Workspace;
import EchoNote.Mihail.Summarizer;
import EchoNote.Mihail.Transcriber;


public class AppConfig {

    private final Workspace workspace;
    private final Transcriber transcriber;
    private final Summarizer summarizer;
    private final ExportService exportService;
    private final SearchService searchService;
    private final EmailNotifier emailNotifier;

    public AppConfig() {
        this.workspace = new Workspace();

        this.transcriber = new Transcriber();
        this.summarizer = new Summarizer();

        this.exportService = new ExportService();
        this.searchService = new SearchService(workspace);
        this.emailNotifier = new EmailNotifier();
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

    public EmailNotifier getEmailNotifier() {
        return emailNotifier;
    }
}
