package EchoNote.Mihail;

import EchoNote.Jack.ActionItem;
import EchoNote.Jack.ActionStatus;
import EchoNote.Jack.Participant;
import EchoNote.Jack.Summary;
import EchoNote.Jack.Transcript;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Summarizer {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-mini";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public Summarizer() {
        this(resolveApiKey());
    }

    public Summarizer(String apiKey) {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    public Summary summarize(Transcript transcript) {
        if (transcript == null) {
            throw new IllegalArgumentException("transcript cannot be null");
        }

        String text = transcript.getRawText();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("transcript raw text cannot be blank");
        }

        String prompt = """
                You are a meeting summarizer. The meeting can be about any topic (school, work, language class, etc.).
                Given the full transcript below, produce a concise structured summary.

                Return your answer as a JSON object with the following fields:
                - "topics": an array of short bullet-like strings summarizing main topics.
                - "decisions": an array of short bullet-like strings summarizing key decisions or conclusions.
                - "notes": a single string with any additional important context, paraphrased in the same language as the transcript when possible.

                Transcript:
                """ + text;

        JsonNode result = callChatApiForJson(prompt);

        List<String> topics = readStringList(result, "topics");
        List<String> decisions = readStringList(result, "decisions");
        String notes = result.has("notes") ? result.get("notes").asText() : "";

        return new Summary(topics, decisions, notes);
    }

    public List<ActionItem> extractActions(Transcript transcript) {
        if (transcript == null) {
            throw new IllegalArgumentException("transcript cannot be null");
        }

        String text = transcript.getRawText();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("transcript raw text cannot be blank");
        }

        String prompt = """
                You are an assistant that extracts action items from meeting transcripts.

                From the transcript below, identify all clear action items. For each action item,
                produce an object with:
                - "title": short imperative phrase describing the task.
                - "owner": name of the person responsible (if unclear, use "Unassigned").
                - "dueDate": ISO date (YYYY-MM-DD) if a specific deadline is mentioned; otherwise null.

                Return your answer as a JSON array of objects.

                Transcript:
                """ + text;

        JsonNode arrayNode = callChatApiForJson(prompt);

        if (!arrayNode.isArray()) {
            throw new SummarizationException("Expected JSON array for action items");
        }

        List<ActionItem> items = new ArrayList<>();

        for (JsonNode node : arrayNode) {
            String title = node.has("title") ? node.get("title").asText() : "";
            if (title == null || title.isBlank()) {
                continue;
            }

            String ownerName = node.has("owner") ? node.get("owner").asText() : "Unassigned";
            String dueDateText = node.has("dueDate") && !node.get("dueDate").isNull()
                    ? node.get("dueDate").asText()
                    : null;

            Participant owner = new Participant(ownerName, null, null);

            LocalDate dueDate = null;
            if (dueDateText != null && !dueDateText.isBlank()) {
                try {
                    dueDate = LocalDate.parse(dueDateText);
                } catch (Exception ignored) {
                }
            }

            ActionItem item = new ActionItem(title, owner, dueDate, ActionStatus.OPEN);
            items.add(item);
        }

        return items;
    }

    private JsonNode callChatApiForJson(String prompt) {
        try {
            Map<String, Object> payload = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of("role", "system",
                                    "content", "You are a helpful assistant that always responds with valid JSON only."),
                            Map.of("role", "user",
                                    "content", prompt)
                    ),
                    "temperature", 0.2
            );

            String jsonPayload = objectMapper.writeValueAsString(payload);

            RequestBody body = RequestBody.create(
                    jsonPayload,
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(CHAT_COMPLETIONS_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new SummarizationException("Chat completion failed: HTTP " +
                            response.code() + " - " + errorBody);
                }

                String responseJson = response.body() != null ? response.body().string() : "";
                JsonNode root = objectMapper.readTree(responseJson);
                JsonNode choices = root.get("choices");
                if (choices == null || !choices.isArray() || choices.isEmpty()) {
                    throw new SummarizationException("Chat completion returned no choices");
                }

                String content = choices.get(0)
                        .get("message")
                        .get("content")
                        .asText();

                return objectMapper.readTree(content);
            }
        } catch (IOException e) {
            throw new SummarizationException("Error calling OpenAI chat API", e);
        }
    }

    private List<String> readStringList(JsonNode node, String fieldName) {
        List<String> result = new ArrayList<>();
        JsonNode arr = node.get(fieldName);
        if (arr != null && arr.isArray()) {
            for (JsonNode el : arr) {
                result.add(el.asText());
            }
        }
        return result;
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
