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
        final boolean enableReset = true;
        if(!message.startsWith("$")) return;

        final var nearbyVillagers = getNearbyVillagers(player);

        var configuredVillagers = Main.CONFIG_MANAGER.getVillagers();
        Main.LOGGER.info("Found {} villagers.", nearbyVillagers.size());
        for (final var villager : nearbyVillagers) {
            if(!Main.VILLAGER_MANAGER.isVillagerConfigured(villager.getUUID()) || enableReset) {
                final var optionalMemory = Main.VILLAGER_MANAGER.addNewVillager(villager, enableReset);
                if(optionalMemory.isPresent()) {
                    final var memory = optionalMemory.get();
                    player.sendSystemMessage(Component.literal("Found new villager called " + memory.getName()));
                    villager.setCustomName(Component.literal(memory.getName()));
                }
            }
        }

        Main.CONFIG_MANAGER.setVillagers(configuredVillagers);
    }

    private static List<Villager> getNearbyVillagers(ServerPlayer player) {
        // Find the villagers nearby
        final var position = player.getEyePosition();
        final var posA = position.add(-10, -10, -10);
        final var posB = position.add(10, 10, 10);
        final var nearbyVillagers = player.getCommandSenderWorld().getEntitiesOfClass(Villager.class, new AABB(posA, posB));
        return nearbyVillagers;
    }
}
