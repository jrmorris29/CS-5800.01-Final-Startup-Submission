package EchoNote.Config;

import io.github.cdimascio.dotenv.Dotenv;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class OpenAiClientFactory {

    private static final OpenAIClient CLIENT = createClient();

    private static OpenAIClient createClient() {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .load();
        String apiKey = dotenv.get("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not set in .env");
        }

        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public static OpenAIClient getClient() {
        return CLIENT;
    }
}