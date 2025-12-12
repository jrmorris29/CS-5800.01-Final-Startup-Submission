package EchoNote.Mihail;

/**
 * Exception thrown when the email draft service fails to open the mail client.
 */
public class EmailDraftException extends RuntimeException {

    public EmailDraftException(String message) {
        super(message);
    }

    public EmailDraftException(String message, Throwable cause) {
        super(message, cause);
    }
}
