package EchoNote.Mihail;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TranscriberTest {

    @Test
    void transcribeFile_nullPath_throwsIllegalArgumentException() {
        Transcriber transcriber = new Transcriber("dummy-api-key");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transcriber.transcribeFile(null),
                "Passing null wavFile should throw IllegalArgumentException"
        );

        assertTrue(
                ex.getMessage().startsWith("wavFile must exist"),
                "Exception message should indicate that wavFile must exist"
        );
    }

    @Test
    void transcribeFile_nonExistentPath_throwsIllegalArgumentException() {
        Transcriber transcriber = new Transcriber("dummy-api-key");

        Path nonExistent = Path.of("this_file_should_not_exist_123456789.wav");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transcriber.transcribeFile(nonExistent),
                "Passing a non-existent wavFile should throw IllegalArgumentException"
        );

        assertTrue(
                ex.getMessage().contains(nonExistent.toString()),
                "Exception message should include the offending path"
        );
    }
}
