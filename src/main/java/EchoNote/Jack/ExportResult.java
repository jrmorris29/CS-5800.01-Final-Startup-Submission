package EchoNote.Jack;

public class ExportResult {
    private final boolean success;
    private final String link;
    private final String message;

    public ExportResult(boolean success, String link, String message) {
        this.success = success;
        this.link = link;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getLink() {
        return link;
    }

    public String getMessage() {
        return message;
    }
}
