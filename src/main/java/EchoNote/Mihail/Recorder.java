package EchoNote.Mihail;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.DoubleConsumer;

/**
 * Records audio from the microphone and saves it as WAV files.
 * Supports both timed recording and interactive start/stop recording.
 */
public class Recorder {

    private static final int BUFFER_SIZE = 4096;
    private static final int SAMPLE_RATE = 44_100;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = 2;

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE,
            SAMPLE_SIZE_BITS,
            CHANNELS,
            FRAME_SIZE,
            SAMPLE_RATE,
            false
    );

    private volatile boolean interactiveRecording = false;
    private Thread interactiveThread;
    private Path interactiveOutputFile;
    private Exception interactiveError;

    public Path recordToFile(Path outputFile, Duration maxDuration) {
        return recordToFile(outputFile, maxDuration, null);
    }

    public Path recordToFile(Path outputFile, Duration maxDuration, DoubleConsumer levelCallback) {
        ensureDirectoryExists(outputFile.getParent());

        TargetDataLine microphone = openMicrophone();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final boolean[] running = {true};

        startStopperThread(maxDuration, microphone, running);
        microphone.start();

        recordAudioToBuffer(microphone, buffer, running, levelCallback);
        writeWavFile(buffer.toByteArray(), outputFile);

        return outputFile;
    }

    public Path recordToTempFile(String filePrefix, Duration maxDuration) {
        return recordToTempFile(filePrefix, maxDuration, null);
    }

    public Path recordToTempFile(String filePrefix, Duration maxDuration, DoubleConsumer levelCallback) {
        try {
            Path temp = Files.createTempFile(filePrefix, ".wav");
            return recordToFile(temp, maxDuration, levelCallback);
        } catch (IOException e) {
            throw new TranscriptionException("Unable to create temp file for recording", e);
        }
    }

    public synchronized void startInteractiveRecording(String filePrefix, DoubleConsumer levelCallback) {
        if (interactiveRecording) {
            throw new IllegalStateException("Already recording");
        }

        interactiveRecording = true;
        interactiveError = null;
        interactiveOutputFile = createRecordingOutputFile(filePrefix);

        interactiveThread = new Thread(() -> runInteractiveRecording(levelCallback), "Recorder-Interactive");
        interactiveThread.start();
    }

    public synchronized Path stopInteractiveRecording() {
        validateInteractiveRecordingState();

        interactiveRecording = false;

        try {
            interactiveThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TranscriptionException("Interrupted while stopping recording", e);
        }

        if (interactiveError != null) {
            Exception ex = interactiveError;
            interactiveError = null;
            throw new TranscriptionException("Error during recording", ex);
        }

        Path result = interactiveOutputFile;
        interactiveThread = null;
        interactiveOutputFile = null;
        return result;
    }

    private void ensureDirectoryExists(Path directory) {
        if (directory == null) {
            return;
        }
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new TranscriptionException("Unable to create directory " + directory, e);
        }
    }

    private TargetDataLine openMicrophone() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(AUDIO_FORMAT);
            return microphone;
        } catch (LineUnavailableException e) {
            throw new TranscriptionException("Microphone line unavailable", e);
        }
    }

    private void startStopperThread(Duration maxDuration, TargetDataLine microphone, boolean[] running) {
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(maxDuration.toMillis());
            } catch (InterruptedException ignored) {
                // Intentionally empty
            }
            running[0] = false;
            microphone.stop();
            microphone.close();
        }, "Recorder-Stopper");

        stopper.start();
    }

    private void recordAudioToBuffer(TargetDataLine microphone, ByteArrayOutputStream buffer,
                                     boolean[] running, DoubleConsumer levelCallback) {
        byte[] data = new byte[BUFFER_SIZE];

        while (running[0]) {
            int bytesRead = microphone.read(data, 0, data.length);
            if (bytesRead <= 0) {
                break;
            }

            buffer.write(data, 0, bytesRead);
            notifyLevelCallback(levelCallback, data, bytesRead);
        }
    }

    private void notifyLevelCallback(DoubleConsumer levelCallback, byte[] data, int bytesRead) {
        if (levelCallback != null) {
            double level = computeLevelRms(data, bytesRead);
            try {
                levelCallback.accept(level);
            } catch (Exception ignored) {
                // Intentionally empty
            }
        }
    }

    private void writeWavFile(byte[] audioBytes, Path outputFile) {
        long frameCount = calculateFrameCount(audioBytes);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
             AudioInputStream ais = new AudioInputStream(bais, AUDIO_FORMAT, frameCount)) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile.toFile());
        } catch (IOException e) {
            throw new TranscriptionException("Error while writing WAV file " + outputFile, e);
        }
    }

    private long calculateFrameCount(byte[] audioBytes) {
        int frameSize = AUDIO_FORMAT.getFrameSize();
        return frameSize > 0 ? audioBytes.length / frameSize : audioBytes.length;
    }

    private double computeLevelRms(byte[] data, int length) {
        if (length <= 0) {
            return 0.0;
        }

        int sampleCount = length / 2;
        if (sampleCount == 0) {
            return 0.0;
        }

        double sumSquares = 0.0;
        for (int i = 0; i < length; i += 2) {
            int low = data[i] & 0xFF;
            int high = data[i + 1];
            int sample = (high << 8) | low;
            double normalized = sample / 32768.0;
            sumSquares += normalized * normalized;
        }

        double rms = Math.sqrt(sumSquares / sampleCount);
        return Math.min(1.0, Math.max(0.0, rms));
    }

    private Path createRecordingOutputFile(String filePrefix) {
        try {
            Path recordingsDir = Path.of("recordings");
            Files.createDirectories(recordingsDir);

            String filename = filePrefix + System.currentTimeMillis() + ".wav";
            return recordingsDir.resolve(filename);
        } catch (IOException e) {
            interactiveRecording = false;
            throw new TranscriptionException("Unable to create recordings directory", e);
        }
    }

    private void runInteractiveRecording(DoubleConsumer levelCallback) {
        TargetDataLine microphone = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            microphone = openMicrophone();
            microphone.start();

            byte[] data = new byte[BUFFER_SIZE];
            while (interactiveRecording) {
                int bytesRead = microphone.read(data, 0, data.length);
                if (bytesRead <= 0) {
                    continue;
                }

                buffer.write(data, 0, bytesRead);
                notifyLevelCallback(levelCallback, data, bytesRead);
            }

            closeMicrophone(microphone);
            microphone = null;

            writeWavFile(buffer.toByteArray(), interactiveOutputFile);
        } catch (Exception ex) {
            synchronized (this) {
                interactiveError = ex;
            }
            closeMicrophoneSafely(microphone);
        }
    }

    private void closeMicrophone(TargetDataLine microphone) {
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
    }

    private void closeMicrophoneSafely(TargetDataLine microphone) {
        if (microphone != null) {
            try {
                microphone.stop();
                microphone.close();
            } catch (Exception ignored) {
                // Intentionally empty
            }
        }
    }

    private void validateInteractiveRecordingState() {
        if (!interactiveRecording || interactiveThread == null || interactiveOutputFile == null) {
            throw new IllegalStateException("Not currently recording");
        }
    }
}
