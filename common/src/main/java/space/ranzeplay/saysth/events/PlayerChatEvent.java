package space.ranzeplay.saysth.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.time.StopWatch;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.io.IOException;
import java.util.List;

public class PlayerChatEvent {
    public static void onPlayerChat(ServerPlayer player, String message) throws IOException {
        if (!message.startsWith(Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix())) return;

        final var nearbyVillagers = getNearbyVillagers(player);

        for (final var villager : nearbyVillagers) {
            final var memory = Main.VILLAGER_MANAGER.getVillager(villager);
            villager.setCustomName(Component.literal(memory.getName()));

            performAIChat(player, message, memory);
        }
    }

    private static void performAIChat(ServerPlayer player, String message, VillagerMemory memory) {
        new Thread(() -> {
            var stopwatch = StopWatch.createStarted();
            try {
                var response = Main.VILLAGER_MANAGER.sendMessageToVillager(memory.getId(), player.getUUID(), message);
                response.ifPresentOrElse(responseMessage -> {
                    if (!responseMessage.equals("IGN")) {
                        player.sendSystemMessage(Component
                                .literal(String.format("<(Villager) %s> %s", memory.getName(), responseMessage))
                                .setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.AQUA)))
                        );

                    }
                }, () -> player.sendSystemMessage(Component
                            .literal(String.format("<(Villager) %s> Failed to response.", memory.getName()))
                            .setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED)))
                ));
            } catch (IOException e) {
                Main.LOGGER.warn(e.getMessage());
            }
            stopwatch.stop();

            if (Main.CONFIG_MANAGER.getConfig().isShowTimeConsumption()) {
                player.sendSystemMessage(Component.literal(String.format("Time consumed: %dms",stopwatch.getTime())));
            }
        }).start();
    }

    private static List<Villager> getNearbyVillagers(ServerPlayer player) {
        final var look = player.getLookAngle();
        final var position = player.getEyePosition().add(look.scale(4));
        final var posA = position.add(-5, -5, -5);
        final var posB = position.add(5, 5, 5);
        return player.getCommandSenderWorld().getEntitiesOfClass(Villager.class, new AABB(posA, posB));
    }
}
