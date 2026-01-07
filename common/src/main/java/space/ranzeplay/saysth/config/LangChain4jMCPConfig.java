package space.ranzeplay.saysth.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import lombok.Data;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for using LangChain4j with MCP (Model Context Protocol) support.
 * This enables integration with various LLM providers through LangChain4j's unified interface.
 */
@Data
public class LangChain4jMCPConfig implements IApiEndpointConfig {
    /**
     * The base URL for the OpenAI API or compatible endpoint
     */
    String baseUrl;
    
    /**
     * API key for authentication
     */
    String apiKey;
    
    /**
     * The model name to use (e.g., "gpt-4", "gpt-3.5-turbo")
     */
    String modelName;
    
    /**
     * Temperature for response generation (0.0 to 2.0)
     */
    Double temperature;
    
    /**
     * Maximum number of tokens to generate
     */
    Integer maxTokens;

    @Override
    public HttpRequest.Builder getPartialHttpRequest() {
        // This method is not used by LangChain4j integration
        // Return a no-op builder for interface compatibility
        return HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://not-used.localhost"))
                .header("Content-Type", "application/json");
    }

    @Override
    public Optional<String> sendConversationAndGetResponseText(Conversation conversation) {
        if (conversation == null || conversation.messages == null || conversation.messages.isEmpty()) {
            Main.LOGGER.warn("Cannot send empty conversation");
            return Optional.empty();
        }

        try {
            // Build the chat model
            ChatLanguageModel chatModel = createChatLanguageModel();
            
            // Convert our messages to LangChain4j format
            List<ChatMessage> langChainMessages = convertMessages(conversation.messages);
            
            // Generate response
            Main.LOGGER.debug("Sending conversation to LangChain4j chat model");
            dev.langchain4j.model.output.Response<AiMessage> response = chatModel.generate(langChainMessages);
            
            String responseText = response.content().text();
            Main.LOGGER.debug("Received response from LangChain4j: {}", responseText);
            
            return Optional.of(responseText);
        } catch (Exception e) {
            Main.LOGGER.error("Failed to send conversation via LangChain4j", e);
            return Optional.empty();
        }
    }

    /**
     * Creates a ChatLanguageModel based on the configuration
     */
    private ChatLanguageModel createChatLanguageModel() {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName != null ? modelName : "gpt-3.5-turbo")
                .timeout(Duration.ofSeconds(Main.CONFIG_MANAGER.getConfig().getTimeoutSeconds()));
        
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            builder.baseUrl(baseUrl);
        }
        
        if (temperature != null) {
            builder.temperature(temperature);
        }
        
        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }
        
        return builder.build();
    }

    /**
     * Converts our internal Message format to LangChain4j's ChatMessage format
     */
    private List<ChatMessage> convertMessages(ArrayList<Message> messages) {
        List<ChatMessage> langChainMessages = new ArrayList<>();
        
        for (Message message : messages) {
            ChatRole role = message.getRole();
            String content = message.getContent();
            
            ChatMessage langChainMessage = switch (role) {
                case SYSTEM -> SystemMessage.from(content);
                case USER -> UserMessage.from(content);
                case ASSISTANT -> AiMessage.from(content);
            };
            
            langChainMessages.add(langChainMessage);
        }
        
        return langChainMessages;
    }
}
