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
        if (conversation == null || conversation.messages == null || conversation.messages.isEmpty()) {
            Main.LOGGER.warn("Cannot send empty conversation");
            return Optional.empty();
        }
        
        Main.LOGGER.debug("Using Cloudflare AI Worker model: {}", modelName);
        var gson = new Gson();
        var conversationJson = gson.toJson(conversation);
        Main.LOGGER.debug("Sending conversation to Cloudflare AI Worker: {}", conversationJson);
        var request = getPartialHttpRequest()
                .POST(HttpRequest.BodyPublishers.ofString(conversationJson))
                .build();

        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            Main.LOGGER.debug("Received response from Cloudflare AI Worker: [{}] {}", response.statusCode(), response.body());
        } catch (IOException | InterruptedException e) {
            Main.LOGGER.error("Failed to send conversation: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
            return Optional.empty();
        }

        if(response.statusCode() != 200) {
            Main.LOGGER.error("Failed to send conversation: [{}] {}", response.statusCode(), response.body());
            return Optional.empty();
        }

        try {
            var responseBody = gson.fromJson(response.body(), JsonObject.class);
            if (responseBody == null || !responseBody.has("result")) {
                Main.LOGGER.error("Invalid response format: missing 'result' field");
                return Optional.empty();
            }
            
            var result = responseBody.get("result").getAsJsonObject();
            if (!result.has("response")) {
                Main.LOGGER.error("Invalid response format: missing 'response' field in result");
                return Optional.empty();
            }
            
            return Optional.of(result.get("response").getAsString());
        } catch (Exception e) {
            Main.LOGGER.error("Failed to parse Cloudflare response: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
