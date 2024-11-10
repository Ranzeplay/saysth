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

        String responseBody;
        try {
            responseBody = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }

        var response = gson.fromJson(responseBody, JsonObject.class);
        return Optional.of(response
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
