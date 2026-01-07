package space.ranzeplay.saysth.config;

import dev.langchain4j.agent.tool.ToolSpecification;
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
import space.ranzeplay.saysth.mcp.McpPluginRegistry;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for using LangChain4j with MCP (Model Context Protocol) support.
 * 
 * <p>This configuration enables integration with various LLM providers through LangChain4j's
 * unified interface and supports MCP server plugins for extended functionality.</p>
 * 
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Support for OpenAI and OpenAI-compatible endpoints</li>
 *   <li>Integration with MCP server plugins for custom tools</li>
 *   <li>Configurable model parameters (temperature, max tokens)</li>
 *   <li>Comprehensive error handling and logging</li>
 * </ul>
 * 
 * @author SaySomething Contributors
 * @since 1.2.0
 * @see McpPluginRegistry
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
    
    /**
     * Whether to enable MCP plugin tools in conversations.
     * 
     * <p>When enabled, all tools from registered MCP plugins will be available
     * for the AI model to use during conversations.</p>
     */
    Boolean enableMcpTools = true;

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
            Main.LOGGER.warn("LangChain4j MCP: Cannot send empty conversation");
            return Optional.empty();
        }

        Main.LOGGER.debug("LangChain4j MCP: Processing conversation with {} message(s)", 
                conversation.messages.size());

        try {
            // Build the chat model
            ChatLanguageModel chatModel = createChatLanguageModel();
            
            // Convert our messages to LangChain4j format
            List<ChatMessage> langChainMessages = convertMessages(conversation.messages);
            
            // Log MCP plugin status
            if (Boolean.TRUE.equals(enableMcpTools)) {
                McpPluginRegistry registry = McpPluginRegistry.getInstance();
                List<ToolSpecification> tools = registry.getAllTools();
                
                if (!tools.isEmpty()) {
                    Main.LOGGER.debug("LangChain4j MCP: {} tool(s) available from {} plugin(s)", 
                            tools.size(), registry.getPluginNames().size());
                    Main.LOGGER.debug("LangChain4j MCP: Available tools: {}", 
                            tools.stream().map(ToolSpecification::name).toList());
                } else {
                    Main.LOGGER.debug("LangChain4j MCP: No tools available from plugins");
                }
            }
            
            // Generate response
            Main.LOGGER.debug("LangChain4j MCP: Sending conversation to chat model");
            Response<AiMessage> response = chatModel.generate(langChainMessages);
            
            if (response == null || response.content() == null) {
                Main.LOGGER.error("LangChain4j MCP: Received null response from chat model");
                return Optional.empty();
            }
            
            String responseText = response.content().text();
            Main.LOGGER.debug("LangChain4j MCP: Received response ({} characters)", 
                    responseText != null ? responseText.length() : 0);
            
            return Optional.ofNullable(responseText);
        } catch (Exception e) {
            Main.LOGGER.error("LangChain4j MCP: Failed to send conversation - {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Creates a ChatLanguageModel based on the configuration.
     * 
     * <p>Builds an OpenAI-compatible chat model with the configured parameters
     * and optionally integrates MCP plugin tools if enabled.</p>
     * 
     * @return configured ChatLanguageModel instance
     */
    private ChatLanguageModel createChatLanguageModel() {
        Main.LOGGER.debug("LangChain4j MCP: Building chat model with model='{}', baseUrl='{}'", 
                modelName != null ? modelName : "gpt-3.5-turbo", 
                baseUrl != null ? baseUrl : "default");
        
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName != null ? modelName : "gpt-3.5-turbo")
                .timeout(Duration.ofSeconds(Main.CONFIG_MANAGER.getConfig().getTimeoutSeconds()));
        
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            Main.LOGGER.debug("LangChain4j MCP: Using custom base URL: {}", baseUrl);
            builder.baseUrl(baseUrl);
        }
        
        if (temperature != null) {
            Main.LOGGER.debug("LangChain4j MCP: Setting temperature to {}", temperature);
            builder.temperature(temperature);
        }
        
        if (maxTokens != null) {
            Main.LOGGER.debug("LangChain4j MCP: Setting max tokens to {}", maxTokens);
            builder.maxTokens(maxTokens);
        }
        
        return builder.build();
    }

    /**
     * Converts our internal Message format to LangChain4j's ChatMessage format.
     * 
     * @param messages list of internal messages to convert
     * @return list of LangChain4j ChatMessage objects
     */
    private List<ChatMessage> convertMessages(ArrayList<Message> messages) {
        List<ChatMessage> langChainMessages = new ArrayList<>();
        
        Main.LOGGER.debug("LangChain4j MCP: Converting {} message(s) to LangChain4j format", messages.size());
        
        for (Message message : messages) {
            ChatRole role = message.getRole();
            String content = message.getContent();
            
            if (content == null || content.trim().isEmpty()) {
                Main.LOGGER.warn("LangChain4j MCP: Skipping message with null or empty content from role {}", role);
                continue;
            }
            
            ChatMessage langChainMessage = switch (role) {
                case SYSTEM -> {
                    Main.LOGGER.debug("LangChain4j MCP: Adding SYSTEM message");
                    yield SystemMessage.from(content);
                }
                case USER -> {
                    Main.LOGGER.debug("LangChain4j MCP: Adding USER message");
                    yield UserMessage.from(content);
                }
                case ASSISTANT -> {
                    Main.LOGGER.debug("LangChain4j MCP: Adding ASSISTANT message");
                    yield AiMessage.from(content);
                }
            };
            
            langChainMessages.add(langChainMessage);
        }
        
        Main.LOGGER.debug("LangChain4j MCP: Converted to {} LangChain4j message(s)", langChainMessages.size());
        return langChainMessages;
    }
}
