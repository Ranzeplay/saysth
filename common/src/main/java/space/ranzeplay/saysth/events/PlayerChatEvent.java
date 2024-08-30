package space.ranzeplay.saysth.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.VillagerMemory;

import java.io.IOException;
import java.util.HashMap;

public class PlayerChatEvent {
    public static void onPlayerChat(ServerPlayer player, String message) throws IOException {
        if(!message.startsWith("$")) return;

        // Find the villagers nearby
        final var position = player.getEyePosition();
        final var posA = position.add(-10, -10, -10);
        final var posB = position.add(10, 10, 10);
        final var nearbyVillagers = player.getCommandSenderWorld().getEntitiesOfClass(Villager.class, new AABB(posA, posB));

        var existingVillagers = Main.CONFIG_MANAGER.getVillagers();
        Main.LOGGER.info("Found {} villagers.", nearbyVillagers.size());
        for (final var villager : nearbyVillagers) {
            final var candidates = Main.CONFIG_MANAGER.getConfig().getNameCandidates();
            final var newName = candidates[(int) (Math.random() * 100000) % candidates.length];
            player.getServer().sendSystemMessage(Component.literal("Found new villager called " + newName));
            villager.setCustomName(Component.literal(newName));
            existingVillagers.put(villager.getUUID(), new VillagerMemory(villager.getUUID(), newName, new HashMap<>()));
        }

        Main.CONFIG_MANAGER.setVillagers(existingVillagers);
    }
}
