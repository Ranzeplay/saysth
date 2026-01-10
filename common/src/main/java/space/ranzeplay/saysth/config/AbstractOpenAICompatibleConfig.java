package space.ranzeplay.saysth.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

public abstract class AbstractOpenAICompatibleConfig implements IApiEndpointConfig {
    String authCredentials;
    String modelName;

    abstract String getChatCompletionEndpoint();
    @SuppressWarnings("unused")
    abstract String buildAuthCredentials();

    @Override
    public HttpRequest.Builder getPartialHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(getChatCompletionEndpoint()))
                .header("Content-Type", "application/json")
                .header("Authorization", authCredentials)
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(Main.CONFIG_MANAGER.getConfig().getTimeoutSeconds()));
    }

    @Override
    public Optional<String> sendConversationAndGetResponseText(Conversation conversation) {
        if (conversation == null || conversation.messages == null || conversation.messages.isEmpty()) {
            Main.LOGGER.warn("Cannot send empty conversation");
            return Optional.empty();
        }
        
        var gson = new Gson();
        var openAIConversation = new OpenAIConversation(modelName, conversation.messages);
        var conversationJson = gson.toJson(openAIConversation);
        Main.LOGGER.debug("Sending conversation to OpenAI-compatible endpoint: {}", conversationJson);
        var request = getPartialHttpRequest()
                .POST(HttpRequest.BodyPublishers.ofString(conversationJson))
                .build();


        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            Main.LOGGER.debug("Received response from OpenAI-compatible endpoint: [{}] {}", response.statusCode(), response.body());
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
            if (responseBody == null || !responseBody.has("choices")) {
                Main.LOGGER.error("Invalid response format: missing 'choices' field");
                return Optional.empty();
            }
            
            var choices = responseBody.get("choices").getAsJsonArray();
            if (choices.isEmpty()) {
                Main.LOGGER.error("Empty choices array in response");
                return Optional.empty();
            }
            
            var firstChoice = choices.get(0).getAsJsonObject();
            if (!firstChoice.has("message")) {
                Main.LOGGER.error("Invalid response format: missing 'message' field in choice");
                return Optional.empty();
            }
            
            var message = firstChoice.get("message").getAsJsonObject();
            if (!message.has("content")) {
                Main.LOGGER.error("Invalid response format: missing 'content' field in message");
                return Optional.empty();
            }
            
            return Optional.of(message.get("content").getAsString());
        } catch (Exception e) {
            Main.LOGGER.error("Failed to parse response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @AllArgsConstructor
    static class OpenAIConversation {
        @SuppressWarnings("unused")
        String model;
        @SuppressWarnings("unused")
        ArrayList<Message> messages;
    }
}
