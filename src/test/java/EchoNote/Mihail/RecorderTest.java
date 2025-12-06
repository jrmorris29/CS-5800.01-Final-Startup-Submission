package EchoNote.Mihail;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RecorderTest {

    @Test
    void stopInteractiveRecording_whenNotRecording_throwsIllegalStateException() {
        Recorder recorder = new Recorder();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                recorder::stopInteractiveRecording,
                "Stopping interactive recording when none is active should throw IllegalStateException"
        );

        assertEquals("Not currently recording", ex.getMessage());
    }
}
