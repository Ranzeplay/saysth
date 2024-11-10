package space.ranzeplay.saysth.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.Conversation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Data
public class CloudflareAIWorkerConfig implements IApiEndpointConfig {
    public String modelName;
    public String apiKey;
    public String accountId;

    @Override
    public HttpRequest.Builder getPartialHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://api.cloudflare.com/client/v4/accounts/" + accountId + "/ai/run/" + modelName))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(Main.CONFIG_MANAGER.getConfig().getTimeoutSeconds()));
    }

    @Override
    public Optional<String> sendConversationAndGetResponseText(Conversation conversation) {
        var gson = new Gson();
        var request = getPartialHttpRequest()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(conversation)))
                .build();

        String responseBody;
        try {
            responseBody = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }

        var response = gson.fromJson(responseBody, JsonObject.class);
        return Optional.of(response
                .get("result")
                .getAsJsonObject()
                .get("response")
                .getAsString()
        );
    }
}
