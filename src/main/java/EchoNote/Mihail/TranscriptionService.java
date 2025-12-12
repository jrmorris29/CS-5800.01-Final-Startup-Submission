package EchoNote.Mihail;

import EchoNote.Jack.Transcript;

import java.nio.file.Path;

/**
 * Proxy Pattern: Subject interface for Transcriber and TranscriberProxy.
 */
public interface TranscriptionService {
    Transcript transcribe(Path audioFile);
}
