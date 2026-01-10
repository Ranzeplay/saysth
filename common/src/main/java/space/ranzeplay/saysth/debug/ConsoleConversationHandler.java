package space.ranzeplay.saysth.debug;

import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * Console conversation handler for debugging the mod's conversation feature.
 * Creates a temporary villager conversation that can be interacted with from the server console.
 */
public class ConsoleConversationHandler {
    private static final UUID DEBUG_VILLAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONSOLE_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String DEBUG_VILLAGER_NAME = "Debug Villager";
    private static final int DEBUG_SYSTEM_MESSAGE_COUNT = 2; // Character and debug info
    
    private VillagerMemory debugVillager;
    private boolean initialized = false;

    /**
     * Initialize the debug villager with a temporary conversation
     */
    public void initialize() {
        if (initialized) {
            return;
        }

        Main.LOGGER.info("Initializing console conversation handler for debugging");
        
        // Create a debug villager with random characteristics from config
        var config = Main.CONFIG_MANAGER.getConfig();
        var personalities = config.getPersonalities();
        var nameCandidates = config.getNameCandidates();
        
        String personality = personalities.length > 0 ? personalities[0] : "helpful";
        String name = nameCandidates.length > 0 ? nameCandidates[0] : DEBUG_VILLAGER_NAME;
        
        debugVillager = new VillagerMemory(
            DEBUG_VILLAGER_ID,
            name,
            personality,
            "debug_villager",
            "console",
            new HashMap<>()
        );
        
        // Initialize conversation with console
        debugVillager.addConversation(CONSOLE_PLAYER_ID);
        
        initialized = true;
        
        Main.LOGGER.info("Console conversation handler initialized with villager: {}", name);
        String prefix = Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            Main.LOGGER.info("Use '{}' prefix in console to chat with the debug villager", prefix);
        } else {
            Main.LOGGER.info("No prefix configured - all console input will be processed by the debug villager");
        }
    }

    /**
     * Handle console input and return the villager's response
     * @param input The console input message
     * @return The villager's response, or null if input doesn't match chat prefix
     */
    public String handleConsoleInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            Main.LOGGER.debug("Received null or empty console input");
            return null;
        }
        
        ensureInitialized();

        String message = extractMessageFromInput(input);
        if (message == null) {
            return null; // Input doesn't match prefix
        }
        
        if (message.isEmpty()) {
            Main.LOGGER.info("Empty message after prefix removal");
            return "Debug villager is ready to chat! Type a message after the prefix.";
        }

        try {
            return processConversation(message);
        } catch (Exception e) {
            Main.LOGGER.error("Error handling console conversation: {}", e.getMessage(), e);
            return String.format("<%s> [Error: %s]", debugVillager.getName(), e.getMessage());
        }
    }

    private void ensureInitialized() {
        if (!initialized) {
            Main.LOGGER.info("Console conversation handler not initialized, initializing now");
            initialize();
        }
    }

    private String extractMessageFromInput(String input) {
        String prefix = Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix();
        
        // If prefix is null or empty, match everything (no prefix required)
        // Otherwise, only match messages that start with the prefix
        if (prefix != null && !prefix.isEmpty()) {
            if (!input.startsWith(prefix)) {
                Main.LOGGER.debug("Console input does not start with required prefix '{}': {}", prefix, input);
                return null;
            }
            // Remove prefix from message
            String message = input.substring(prefix.length()).trim();
            Main.LOGGER.debug("Console input matches prefix '{}', extracted message: {}", prefix, message);
            return message;
        } else {
            // No prefix configured, use entire input as message
            Main.LOGGER.debug("No prefix configured, using entire input as message");
            return input.trim();
        }
    }

    private String processConversation(String message) throws IOException {
        Main.LOGGER.debug("Processing console message: {}", message);
        Conversation conversation = debugVillager.getConversation(CONSOLE_PLAYER_ID);
        
        conversation.addMessage(new Message(ChatRole.USER, message));
        Main.LOGGER.debug("Added user message to conversation, total messages: {}", conversation.messages.size());
        
        addSystemMessagesForDebug(conversation);
        
        var response = sendToAIAndGetResponse(conversation);
        if (response.isPresent()) {
            return handleSuccessfulResponse(conversation, response.get());
        } else {
            Main.LOGGER.warn("Failed to get response from AI for debug villager");
            return String.format("<%s> [Failed to get response from AI]", debugVillager.getName());
        }
    }

    private void addSystemMessagesForDebug(Conversation conversation) {
        conversation.messages.addFirst(new Message(ChatRole.SYSTEM, getDebugSystemMessage()));
        conversation.messages.addFirst(new Message(ChatRole.SYSTEM, debugVillager.getCharacter()));
        Main.LOGGER.debug("Added system messages to conversation");
    }

    private Optional<String> sendToAIAndGetResponse(Conversation conversation) {
        Main.LOGGER.info("Sending conversation to AI for debug villager {}", debugVillager.getName());
        return Main.CONFIG_MANAGER.getApiConfig().sendConversationAndGetResponseText(conversation);
    }

    private String handleSuccessfulResponse(Conversation conversation, String responseText) throws IOException {
        Main.LOGGER.info("Received response from AI: {}", responseText);
        
        conversation.addMessage(new Message(ChatRole.ASSISTANT, responseText));
        removeSystemMessagesFromConversation(conversation);
        debugVillager.updateConversation(CONSOLE_PLAYER_ID, conversation);
        concludeConversationIfNeeded(conversation);
        
        return String.format("<%s> %s", debugVillager.getName(), responseText);
    }

    private void removeSystemMessagesFromConversation(Conversation conversation) {
        int removedCount = 0;
        for (int i = 0; i < DEBUG_SYSTEM_MESSAGE_COUNT && !conversation.messages.isEmpty(); i++) {
            if (conversation.messages.getFirst().getRole() == ChatRole.SYSTEM) {
                conversation.messages.removeFirst();
                removedCount++;
            } else {
                break;
            }
        }
        Main.LOGGER.debug("Removed {} system messages from conversation", removedCount);
    }

    private void concludeConversationIfNeeded(Conversation conversation) throws IOException {
        int messageLimit = Main.CONFIG_MANAGER.getConfig().getConclusionMessageLimit();
        if (messageLimit > 0 && conversation.messages.size() > messageLimit) {
            Main.LOGGER.info("Debug conversation reached message limit ({}/{}), concluding...", 
                conversation.messages.size(), messageLimit);
            debugVillager = Main.VILLAGER_MANAGER.concludeMemory(debugVillager, CONSOLE_PLAYER_ID);
        }
    }

    /**
     * Get debug-specific system message
     */
    private String getDebugSystemMessage() {
        return "You are in debug mode. The user is interacting with you through the server console for testing purposes. " +
               "You should respond as a normal villager would, but you can acknowledge that this is a debug session if asked.";
    }

    /**
     * Reset the debug conversation
     */
    public void resetConversation() {
        if (initialized && debugVillager != null) {
            debugVillager.addConversation(CONSOLE_PLAYER_ID); // This replaces the existing conversation
            Main.LOGGER.info("Debug conversation reset");
        }
    }

    /**
     * Get conversation history for debugging
     */
    public String getConversationHistory() {
        if (!initialized || debugVillager == null) {
            return "Console conversation handler not initialized";
        }

        Conversation conversation = debugVillager.getConversation(CONSOLE_PLAYER_ID);
        if (conversation.messages.isEmpty()) {
            return "No conversation history";
        }

        StringBuilder history = new StringBuilder();
        history.append("=== Debug Conversation History ===\n");
        history.append("Villager: ").append(debugVillager.getName()).append("\n");
        history.append("Personality: ").append(debugVillager.getPersonality()).append("\n");
        history.append("Messages: ").append(conversation.messages.size()).append("\n\n");

        for (Message message : conversation.messages) {
            String role = message.getRole() == ChatRole.USER ? "Console" : 
                         message.getRole() == ChatRole.ASSISTANT ? debugVillager.getName() : "System";
            history.append(String.format("[%s]: %s\n", role, message.getContent()));
        }

        return history.toString();
    }

    /**
     * Get current villager info
     */
    public String getVillagerInfo() {
        if (!initialized || debugVillager == null) {
            return "Console conversation handler not initialized";
        }

        return String.format("Debug Villager Info:\n" +
                           "Name: %s\n" +
                           "Personality: %s\n" +
                           "Profession: %s\n" +
                           "Living In: %s\n" +
                           "Conversation Messages: %d\n" +
                           "Chat Prefix: %s",
                           debugVillager.getName(),
                           debugVillager.getPersonality(),
                           debugVillager.getProfession(),
                           debugVillager.getLivingIn(),
                           debugVillager.getConversation(CONSOLE_PLAYER_ID).messages.size(),
                           Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix());
    }
}
