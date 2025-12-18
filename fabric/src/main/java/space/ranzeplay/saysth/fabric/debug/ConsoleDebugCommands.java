package space.ranzeplay.saysth.fabric.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import space.ranzeplay.saysth.Main;

/**
 * Fabric-specific console debug commands for the conversation handler
 */
public class ConsoleDebugCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("saysth-debug")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS))) // Require OP level 4 (server console/admin)
            .then(Commands.literal("chat")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(ConsoleDebugCommands::handleChat)
                )
                .executes(context -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Usage: /saysth-debug chat <message>"), false);
                    return 1;
                })
            )
            .then(Commands.literal("reset")
                .executes(ConsoleDebugCommands::resetConversation)
            )
            .then(Commands.literal("history")
                .executes(ConsoleDebugCommands::showHistory)
            )
            .then(Commands.literal("info")
                .executes(ConsoleDebugCommands::showInfo)
            )
            .then(Commands.literal("help")
                .executes(ConsoleDebugCommands::showHelp)
            )
            .executes(ConsoleDebugCommands::showHelp)
        );

        // Also register a simple chat command using the villager prefix
        dispatcher.register(Commands.literal("saysth-chat")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS)))
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(ConsoleDebugCommands::handleDirectChat)
            )
            .executes(context -> {
                String prefix = Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix();
                context.getSource().sendSuccess(() -> 
                    Component.literal("Usage: /saysth-chat <message> or use prefix '" + prefix + "' in console"), false);
                return 1;
            })
        );
    }    private static int handleChat(CommandContext<CommandSourceStack> context) {
        String message = StringArgumentType.getString(context, "message");
        String prefix = Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix();
        CommandSourceStack source = context.getSource();
        
        // Show immediate confirmation that message was received
        source.sendSuccess(() -> Component.literal("Processing message: " + message), false);
        
        // Execute AI call asynchronously to prevent blocking
        new Thread(() -> {
            try {
                String response = Main.CONSOLE_HANDLER.handleConsoleInput(prefix + message);
                
                if (response != null) {
                    source.sendSuccess(() -> Component.literal(response), false);
                } else {
                    source.sendSuccess(() -> 
                        Component.literal("Error: Could not process message"), false);
                }
            } catch (Exception e) {
                source.sendSuccess(() -> 
                    Component.literal("Error: " + e.getMessage()), false);
                Main.LOGGER.error("Error in async chat handling: {}", e.getMessage(), e);
            }
        }).start();
        
        return 1;
    }    private static int handleDirectChat(CommandContext<CommandSourceStack> context) {
        String message = StringArgumentType.getString(context, "message");
        String prefix = Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix();
        CommandSourceStack source = context.getSource();
        
        // Show immediate confirmation that message was received
        source.sendSuccess(() -> Component.literal("Processing message: " + message), false);
        
        // Execute AI call asynchronously to prevent blocking
        new Thread(() -> {
            try {
                String response = Main.CONSOLE_HANDLER.handleConsoleInput(prefix + message);
                
                if (response != null) {
                    source.sendSuccess(() -> Component.literal(response), false);
                } else {
                    source.sendSuccess(() -> 
                        Component.literal("Error: Could not process message"), false);
                }
            } catch (Exception e) {
                source.sendSuccess(() -> 
                    Component.literal("Error: " + e.getMessage()), false);
                Main.LOGGER.error("Error in async direct chat handling: {}", e.getMessage(), e);
            }
        }).start();
        
        return 1;
    }

    private static int resetConversation(CommandContext<CommandSourceStack> context) {
        Main.CONSOLE_HANDLER.resetConversation();
        context.getSource().sendSuccess(() -> 
            Component.literal("Debug conversation has been reset"), false);
        return 1;
    }

    private static int showHistory(CommandContext<CommandSourceStack> context) {
        String history = Main.CONSOLE_HANDLER.getConversationHistory();
        String[] lines = history.split("\n");
        
        for (String line : lines) {
            context.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        
        return 1;
    }

    private static int showInfo(CommandContext<CommandSourceStack> context) {
        String info = Main.CONSOLE_HANDLER.getVillagerInfo();
        String[] lines = info.split("\n");
        
        for (String line : lines) {
            context.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        
        return 1;
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        String prefix = Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix();
        
        context.getSource().sendSuccess(() -> Component.literal("=== SaysTh Debug Commands ==="), false);
        context.getSource().sendSuccess(() -> Component.literal("/saysth-debug chat <message> - Chat with debug villager"), false);
        context.getSource().sendSuccess(() -> Component.literal("/saysth-debug reset - Reset conversation history"), false);
        context.getSource().sendSuccess(() -> Component.literal("/saysth-debug history - Show conversation history"), false);
        context.getSource().sendSuccess(() -> Component.literal("/saysth-debug info - Show villager information"), false);
        context.getSource().sendSuccess(() -> Component.literal("/saysth-debug help - Show this help"), false);
        context.getSource().sendSuccess(() -> Component.literal(""), false);
        context.getSource().sendSuccess(() -> Component.literal("/saysth-chat <message> - Direct chat (adds prefix automatically)"), false);
        context.getSource().sendSuccess(() -> Component.literal(""), false);
        context.getSource().sendSuccess(() -> Component.literal("Chat Prefix: '" + prefix + "'"), false);
        context.getSource().sendSuccess(() -> Component.literal("You can also type '" + prefix + "<message>' directly in console"), false);
        
        return 1;
    }

    /**
     * Initialize the console handler
     */
    public static void initialize() {
        Main.CONSOLE_HANDLER.initialize();
        Main.LOGGER.info("Console debug commands registered for Fabric");
    }

    /**
     * Handle raw console input (for server console integration)
     */
    public static String handleRawConsoleInput(String input) {
        return Main.CONSOLE_HANDLER.handleConsoleInput(input);
    }
}
