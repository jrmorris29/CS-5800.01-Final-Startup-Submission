package EchoNote.Mihail;

import EchoNote.Jack.Transcript;
import EchoNote.Jack.TranscriptSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class SummarizerTest {

    private Transcript createBlankTranscript() {
        return new Transcript(
                "t1",
                "   ",
                Collections.emptyList(),
                TranscriptSource.LIVE
        );
    }

    @Test
    void summarize_nullTranscript_throwsIllegalArgumentException() {
        Summarizer summarizer = new Summarizer("dummy-api-key");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> summarizer.summarize(null),
                "summarize(null) should throw IllegalArgumentException"
        );

        assertEquals("transcript cannot be null", ex.getMessage());
    }

    @Test
    void summarize_blankTranscript_throwsIllegalArgumentException() {
        Summarizer summarizer = new Summarizer("dummy-api-key");
        Transcript blank = createBlankTranscript();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> summarizer.summarize(blank),
                "summarize with blank transcript text should throw IllegalArgumentException"
        );

        assertEquals("transcript raw text cannot be blank", ex.getMessage());
    }

    @Test
    void extractActions_nullTranscript_throwsIllegalArgumentException() {
        Summarizer summarizer = new Summarizer("dummy-api-key");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> summarizer.extractActions(null),
                "extractActions(null) should throw IllegalArgumentException"
        );

        assertEquals("transcript cannot be null", ex.getMessage());
    }

    @Test
    void extractActions_blankTranscript_throwsIllegalArgumentException() {
        Summarizer summarizer = new Summarizer("dummy-api-key");
        Transcript blank = createBlankTranscript();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> summarizer.extractActions(blank),
                "extractActions with blank transcript text should throw IllegalArgumentException"
        );

        assertEquals("transcript raw text cannot be blank", ex.getMessage());
    }
}
