package space.ranzeplay.saysth.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.time.StopWatch;
import space.ranzeplay.saysth.Main;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerChatEvent {
    public static void onPlayerChat(ServerPlayer player, String message) throws IOException {
        if (player == null) {
            Main.LOGGER.warn("Player is null in chat event");
            return;
        }
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        String chatPrefix = Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix();
        
        // If prefix is null or empty, match everything (no prefix required)
        // Otherwise, only match messages that start with the prefix
        if (chatPrefix != null && !chatPrefix.isEmpty()) {
            if (!message.startsWith(chatPrefix)) {
                Main.LOGGER.debug("Message does not start with required prefix '{}': {}", chatPrefix, message);
                return;
            }
            Main.LOGGER.debug("Message matches prefix '{}': {}", chatPrefix, message);
        } else {
            Main.LOGGER.debug("No prefix configured, processing all messages");
        }

        final var nearbyVillagers = getNearbyVillagers(player);
        if (nearbyVillagers == null || nearbyVillagers.isEmpty()) {
            Main.LOGGER.debug("No nearby villagers found for player {}", player.getName().getString());
            return;
        }
        
        Main.LOGGER.info("Found {} nearby villager(s) for player {}", nearbyVillagers.size(), player.getName().getString());

        for (final var villager : nearbyVillagers) {
            if (villager == null) {
                Main.LOGGER.warn("Encountered null villager in nearby list, skipping");
                continue;
            }
            Main.LOGGER.debug("Processing villager with UUID: {}", villager.getUUID());
            
            final var memory = Main.VILLAGER_MANAGER.getVillager(villager);
            if (memory != null && memory.getName() != null) {
                villager.setCustomName(Component.literal(memory.getName()));
                Main.LOGGER.debug("Set custom name for villager {}: {}", villager.getUUID(), memory.getName());
            }

            performAIChat(player, message, villager);
        }
    }

    private static void performAIChat(ServerPlayer player, String message, Villager villager) {
        if (player == null || villager == null || message == null) {
            Main.LOGGER.warn("Null parameter in performAIChat - player: {}, villager: {}, message: {}", 
                player != null, villager != null, message != null);
            return;
        }
        
        Main.LOGGER.info("Starting AI chat for player {} with villager {}", 
            player.getName().getString(), villager.getUUID());
        
        new Thread(() -> {
            var stopwatch = StopWatch.createStarted();
            try {
                var memory = Main.VILLAGER_MANAGER.getVillager(villager);
                if (memory == null) {
                    Main.LOGGER.warn("Failed to get villager memory for UUID: {}", villager.getUUID());
                    return;
                }
                
                Main.LOGGER.debug("Sending message to villager {}: {}", memory.getName(), message);
                var response = Main.VILLAGER_MANAGER.sendMessageToVillager(villager, player, message);
                response.ifPresentOrElse(responseMessage -> {
                    if (!responseMessage.equals("IGN")) {
                        Main.LOGGER.info("Received response from villager {}: {}", memory.getName(), responseMessage);
                        player.sendSystemMessage(Component
                                .literal(String.format("<(Villager) %s> %s", memory.getName(), responseMessage))
                                .setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.AQUA)))
                        );

                    } else {
                        Main.LOGGER.debug("Villager {} returned IGN response", memory.getName());
                    }
                }, () -> {
                    Main.LOGGER.warn("Failed to get response from villager {}", memory.getName());
                    player.sendSystemMessage(Component
                            .literal(String.format("<(Villager) %s> Failed to response.", memory.getName()))
                            .setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED)))
                    );
                });
            } catch (IOException e) {
                Main.LOGGER.error("Error during AI chat: {}", e.getMessage(), e);
            } catch (Exception e) {
                Main.LOGGER.error("Unexpected error during AI chat: {}", e.getMessage(), e);
            }
            stopwatch.stop();
            long timeMs = stopwatch.getTime(TimeUnit.MILLISECONDS);
            Main.LOGGER.debug("AI chat completed in {}ms", timeMs);

            if (Main.CONFIG_MANAGER.getConfig().isShowTimeConsumption()) {
                player.sendSystemMessage(Component.literal(String.format("Time consumed: %dms", timeMs)));
            }
        }).start();
    }

    private static List<Villager> getNearbyVillagers(ServerPlayer player) {
        if (player == null || player.level() == null) {
            Main.LOGGER.warn("Cannot get nearby villagers - player or level is null");
            return List.of();
        }
        
        try {
            final var look = player.getLookAngle();
            final var position = player.getEyePosition().add(look.scale(4));
            final var posA = position.add(-5, -5, -5);
            final var posB = position.add(5, 5, 5);
            var villagers = player.level().getEntitiesOfClass(Villager.class, new AABB(posA, posB));
            Main.LOGGER.debug("Found {} villagers near player {} in search area", villagers.size(), player.getName().getString());
            return villagers;
        } catch (Exception e) {
            Main.LOGGER.error("Error getting nearby villagers: {}", e.getMessage());
            return List.of();
        }
    }
}
