package EchoNote.Mihail;

import EchoNote.Jack.Transcript;
import EchoNote.Jack.TranscriptSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Proxy Pattern - RealSubject.
 *
 * The actual transcription service that calls the OpenAI Whisper API.
 * This class performs the real work of transcribing audio files.
 */
public class Transcriber implements TranscriptionService {

    private static final String TRANSCRIPTION_URL = "https://api.openai.com/v1/audio/transcriptions";
    private static final MediaType MEDIA_TYPE_WAV = MediaType.parse("audio/wav");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public Transcriber() {
        this(resolveApiKey());
    }

    public Transcriber(String apiKey) {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    @Override
    public Transcript transcribe(Path audioFile) {
        return transcribeFile(audioFile);
    }

    public Transcript transcribeFile(Path wavFile) {
        if (wavFile == null || !Files.exists(wavFile)) {
            throw new IllegalArgumentException("wavFile must exist: " + wavFile);
        }

        RequestBody fileBody = RequestBody.create(wavFile.toFile(), MEDIA_TYPE_WAV);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", wavFile.getFileName().toString(), fileBody)
                .addFormDataPart("model", "whisper-1")
                .build();

        Request request = new Request.Builder()
                .url(TRANSCRIPTION_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new TranscriptionException("Transcription failed: HTTP " +
                        response.code() + " - " + errorBody);
            }

            String json = response.body() != null ? response.body().string() : "";
            JsonNode root = objectMapper.readTree(json);
            String text = root.has("text") ? root.get("text").asText() : "";


            return new Transcript(text, TranscriptSource.LIVE);
        } catch (IOException e) {
            throw new TranscriptionException("Error calling OpenAI transcription API", e);
        }
    }

    private static String resolveApiKey() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key != null && !key.isBlank()) {
            return key;
        }

        try {
            Dotenv dotenv = Dotenv.load();
            key = dotenv.get("OPENAI_API_KEY");
        } catch (Exception ignored) {
        }

        if (key == null || key.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not configured (env or .env)");
        }
        return key;
    }
}
