package space.ranzeplay.saysth.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Tool;
import space.ranzeplay.saysth.chat.ToolCall;
import space.ranzeplay.saysth.chat.ToolResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

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
        return sendConversation(conversation, List.of())
                .filter(response -> !response.hasToolCalls())
                .map(ToolResponse::content);
    }

    @Override
    public Optional<ToolResponse> sendConversation(Conversation conversation, List<Tool> tools) {
        if (conversation == null || conversation.messages == null || conversation.messages.isEmpty()) {
            Main.LOGGER.warn("Cannot send empty conversation");
            return Optional.empty();
        }
        
        Main.LOGGER.debug("Using Cloudflare AI Worker model: {}", modelName);
        var gson = new Gson();
        var toolDefinitions = tools == null ? List.<AbstractOpenAICompatibleConfig.ToolDefinition>of() : tools.stream()
                .map(tool -> new AbstractOpenAICompatibleConfig.ToolDefinition("function",
                        new AbstractOpenAICompatibleConfig.FunctionDefinition(
                                tool.getName(), tool.getDescription(), tool.getParameters())))
                .toList();
        var requestBody = new AbstractOpenAICompatibleConfig.OpenAIConversation(
                modelName, conversation.messages, toolDefinitions);
        var conversationJson = gson.toJson(requestBody);
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
            var toolCalls = new ArrayList<ToolCall>();
            if (result.has("tool_calls") && !result.get("tool_calls").isJsonNull()) {
                for (var toolCall : result.getAsJsonArray("tool_calls")) {
                    toolCalls.add(ToolCall.fromJson(toolCall.getAsJsonObject()));
                }
            }
            var content = result.has("response") && !result.get("response").isJsonNull()
                    ? result.get("response").getAsString()
                    : null;
            if (content == null && toolCalls.isEmpty()) {
                Main.LOGGER.error("Invalid response format: result has neither response nor tool calls");
                return Optional.empty();
            }
            return Optional.of(new ToolResponse(content, toolCalls));
        } catch (Exception e) {
            Main.LOGGER.error("Failed to parse Cloudflare response: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
