package EchoNote.Mihail;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.DoubleConsumer;

public class Recorder {

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44_100.0f,   // sample rate
            16,          // sample size in bits
            1,           // channels
            2,           // frame size
            44_100.0f,   // frame rate
            false        // little-endian
    );


    public Path recordToFile(Path outputFile, Duration maxDuration) {
        return recordToFile(outputFile, maxDuration, null);
    }

    public Path recordToFile(Path outputFile, Duration maxDuration, DoubleConsumer levelCallback) {
        try {
            if (outputFile.getParent() != null) {
                Files.createDirectories(outputFile.getParent());
            }
        } catch (IOException e) {
            throw new TranscriptionException("Unable to create directories for output file " + outputFile, e);
        }

        TargetDataLine microphone;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(AUDIO_FORMAT);
        } catch (LineUnavailableException e) {
            throw new TranscriptionException("Microphone line unavailable", e);
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        final boolean[] running = {true};

        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(maxDuration.toMillis());
            } catch (InterruptedException ignored) {
            }
            running[0] = false;
            microphone.stop();
            microphone.close();
        }, "Recorder-Stopper");

        stopper.start();

        microphone.start();

        byte[] data = new byte[4096];

        while (running[0]) {
            int bytesRead = microphone.read(data, 0, data.length);
            if (bytesRead <= 0) {
                break;
            }

            buffer.write(data, 0, bytesRead);

            if (levelCallback != null) {
                double level = computeLevelRms(data, bytesRead);
                try {
                    levelCallback.accept(level);
                } catch (Exception ignored) {
                }
            }
        }

        byte[] audioBytes = buffer.toByteArray();
        int frameSize = AUDIO_FORMAT.getFrameSize();
        long frameCount = frameSize > 0 ? audioBytes.length / frameSize : audioBytes.length;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
             AudioInputStream ais = new AudioInputStream(bais, AUDIO_FORMAT, frameCount)) {

            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile.toFile());
        } catch (IOException e) {
            throw new TranscriptionException("Error while writing WAV file " + outputFile, e);
        }

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

    private double computeLevelRms(byte[] data, int length) {
        if (length <= 0) {
            return 0.0;
        }

        int sampleCount = length / 2;
        if (sampleCount == 0) return 0.0;

        double sumSquares = 0.0;
        for (int i = 0; i < length; i += 2) {
            int low = data[i] & 0xFF;
            int high = data[i + 1];
            int sample = (high << 8) | low;
            double normalized = sample / 32768.0;
            sumSquares += normalized * normalized;
        }

        double rms = Math.sqrt(sumSquares / sampleCount);
        if (rms < 0) rms = 0;
        if (rms > 1) rms = 1;
        return rms;
    }

    private volatile boolean interactiveRecording = false;
    private Thread interactiveThread;
    private Path interactiveOutputFile;
    private Exception interactiveError;


    public synchronized void startInteractiveRecording(String filePrefix, DoubleConsumer levelCallback) {
        if (interactiveRecording) {
            throw new IllegalStateException("Already recording");
        }

        interactiveRecording = true;
        interactiveError = null;

        try {
            Path recordingsDir = Path.of("recordings");
            Files.createDirectories(recordingsDir);

            String filename = filePrefix + System.currentTimeMillis() + ".wav";
            interactiveOutputFile = recordingsDir.resolve(filename);
        } catch (IOException e) {
            interactiveRecording = false;
            throw new TranscriptionException("Unable to create recordings directory", e);
        }

        interactiveThread = new Thread(() -> {
            TargetDataLine microphone = null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            try {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(AUDIO_FORMAT);
                microphone.start();

                byte[] data = new byte[4096];

                while (interactiveRecording) {
                    int bytesRead = microphone.read(data, 0, data.length);
                    if (bytesRead <= 0) {
                        continue;
                    }

                    buffer.write(data, 0, bytesRead);

                    if (levelCallback != null) {
                        double level = computeLevelRms(data, bytesRead);
                        try {
                            levelCallback.accept(level);
                        } catch (Exception ignored) {
                        }
                    }
                }

                microphone.stop();
                microphone.close();
                microphone = null;

                byte[] audioBytes = buffer.toByteArray();
                int frameSize = AUDIO_FORMAT.getFrameSize();
                long frameCount = frameSize > 0 ? audioBytes.length / frameSize : audioBytes.length;

                try (ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
                     AudioInputStream ais = new AudioInputStream(bais, AUDIO_FORMAT, frameCount)) {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, interactiveOutputFile.toFile());
                }
            } catch (Exception ex) {
                synchronized (Recorder.this) {
                    interactiveError = ex;
                }
                if (microphone != null) {
                    try {
                        microphone.stop();
                        microphone.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }, "Recorder-Interactive");

        interactiveThread.start();
    }

    public synchronized Path stopInteractiveRecording() {
        if (!interactiveRecording || interactiveThread == null || interactiveOutputFile == null) {
            throw new IllegalStateException("Not currently recording");
        }

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
}
