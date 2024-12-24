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
    abstract String buildAuthCredentials();

    @Override
    public HttpRequest.Builder getPartialHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(getChatCompletionEndpoint()))
                .header("Content-Type", "application/json")
                .header("Authorization", authCredentials)
                .timeout(Duration.ofSeconds(Main.CONFIG_MANAGER.getConfig().getTimeoutSeconds()));
    }

    @Override
    public Optional<String> sendConversationAndGetResponseText(Conversation conversation) {
        var gson = new Gson();
        var request = getPartialHttpRequest()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(new OpenAIConversation(modelName, conversation.messages))))
                .build();


        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Main.LOGGER.error("Failed to send conversation: {}", e.getMessage());
            return Optional.empty();
        }

        if(response.statusCode() != 200) {
            Main.LOGGER.error("Failed to send conversation: [{}] {}", response.statusCode(), response.body());
            return Optional.empty();
        }

        var responseBody = gson.fromJson(response.body(), JsonObject.class);
        return Optional.of(responseBody
                .get("choices")
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .get("message")
                .getAsJsonObject()
                .get("content")
                .getAsString()
        );
    }

    @AllArgsConstructor
    static class OpenAIConversation {
        @SuppressWarnings("unused")
        String model;
        @SuppressWarnings("unused")
        ArrayList<Message> messages;
    }
}
