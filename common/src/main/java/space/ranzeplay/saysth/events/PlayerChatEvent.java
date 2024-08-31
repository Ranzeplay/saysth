package space.ranzeplay.saysth.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import space.ranzeplay.saysth.Main;

import java.io.IOException;
import java.util.List;

public class PlayerChatEvent {
    public static void onPlayerChat(ServerPlayer player, String message) throws IOException {
        if (!message.startsWith("$")) return;

        final var nearbyVillagers = getNearbyVillagers(player);

        Main.LOGGER.info("Found {} villagers.", nearbyVillagers.size());
        for (final var villager : nearbyVillagers) {
            final var memory = Main.VILLAGER_MANAGER.getVillager(villager);
            villager.setCustomName(Component.literal(memory.getName()));

            new Thread(() -> {
                final String response;
                try {
                    response = Main.VILLAGER_MANAGER.sendMessageToVillager(memory.getId(), player.getUUID(), message);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                player.sendSystemMessage(Component.literal(String.format("<(Villager) %s> %s", memory.getName(), response)));
            }).start();
        }
    }

    private static List<Villager> getNearbyVillagers(ServerPlayer player) {
        final var look = player.getLookAngle();
        final var position = player.getEyePosition().add(look.scale(10));
        final var posA = position.add(-10, -10, -10);
        final var posB = position.add(10, 10, 10);
        return player.getCommandSenderWorld().getEntitiesOfClass(Villager.class, new AABB(posA, posB));
    }
}
