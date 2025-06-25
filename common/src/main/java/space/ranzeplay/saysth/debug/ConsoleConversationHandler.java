package space.ranzeplay.saysth.debug;

import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Console conversation handler for debugging the mod's conversation feature.
 * Creates a temporary villager conversation that can be interacted with from the server console.
 */
public class ConsoleConversationHandler {
    private static final UUID DEBUG_VILLAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CONSOLE_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String DEBUG_VILLAGER_NAME = "Debug Villager";
    
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
        Main.LOGGER.info("Use '{}' prefix in console to chat with the debug villager", 
                        Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix());
    }

    /**
     * Handle console input and return the villager's response
     * @param input The console input message
     * @return The villager's response, or null if input doesn't match chat prefix
     */
    public String handleConsoleInput(String input) {
        if (!initialized) {
            initialize();
        }

        // Check if input starts with villager chat prefix
        String prefix = Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix();
        if (!input.startsWith(prefix)) {
            return null;
        }

        // Remove prefix from message
        String message = input.substring(prefix.length()).trim();
        if (message.isEmpty()) {
            return "Debug villager is ready to chat! Type a message after the prefix.";
        }

        try {
            // Get conversation
            Conversation conversation = debugVillager.getConversation(CONSOLE_PLAYER_ID);
            
            // Add user message
            conversation.addMessage(new Message(ChatRole.USER, message));
            
            // Add system messages (character description first, then debug info)
            conversation.messages.addFirst(new Message(ChatRole.SYSTEM, getDebugSystemMessage()));
            conversation.messages.addFirst(new Message(ChatRole.SYSTEM, debugVillager.getCharacter()));
            
            // Send to AI and get response
            var response = Main.CONFIG_MANAGER.getApiConfig().sendConversationAndGetResponseText(conversation);
            
            if (response.isPresent()) {
                String responseText = response.get();
                
                // Add assistant response to conversation
                conversation.addMessage(new Message(ChatRole.ASSISTANT, responseText));
                
                // Remove system messages to keep conversation clean
                conversation.messages.removeFirst();
                conversation.messages.removeFirst();
                
                // Update conversation in memory
                debugVillager.updateConversation(CONSOLE_PLAYER_ID, conversation);
                
                // Check if conversation needs to be concluded
                if (conversation.messages.size() > Main.CONFIG_MANAGER.getConfig().getConclusionMessageLimit()) {
                    Main.LOGGER.info("Debug conversation reached message limit, concluding...");
                    debugVillager = Main.VILLAGER_MANAGER.concludeMemory(debugVillager, CONSOLE_PLAYER_ID);
                }
                
                return String.format("<%s> %s", debugVillager.getName(), responseText);
            } else {
                return String.format("<%s> [Failed to get response from AI]", debugVillager.getName());
            }
            
        } catch (Exception e) {
            Main.LOGGER.error("Error handling console conversation: {}", e.getMessage(), e);
            return String.format("<%s> [Error: %s]", debugVillager.getName(), e.getMessage());
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
