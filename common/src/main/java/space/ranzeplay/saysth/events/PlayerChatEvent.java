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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerChatEvent {
    public static void onPlayerChat(ServerPlayer player, String message) throws IOException {
        if (!message.startsWith(Main.CONFIG_MANAGER.getConfig().getVillagerChatPrefix())) return;

        final var nearbyVillagers = getNearbyVillagers(player);

        for (final var villager : nearbyVillagers) {
            final var memory = Main.VILLAGER_MANAGER.getVillager(villager);
            villager.setCustomName(Component.literal(memory.getName()));

            performAIChat(player, message, villager);
        }
    }

    private static void performAIChat(ServerPlayer player, String message, Villager villager) {
        new Thread(() -> {
            var stopwatch = StopWatch.createStarted();
            try {
                var memory = Main.VILLAGER_MANAGER.getVillager(villager);
                var response = Main.VILLAGER_MANAGER.sendMessageToVillager(villager, player, message);
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
                player.sendSystemMessage(Component.literal(String.format("Time consumed: %dms",stopwatch.getTime(TimeUnit.MILLISECONDS))));
            }
        }).start();
    }

    private static List<Villager> getNearbyVillagers(ServerPlayer player) {
        final var look = player.getLookAngle();
        final var position = player.getEyePosition().add(look.scale(4));
        final var posA = position.add(-5, -5, -5);
        final var posB = position.add(5, 5, 5);
        return player.level().getEntitiesOfClass(Villager.class, new AABB(posA, posB));
    }
}
