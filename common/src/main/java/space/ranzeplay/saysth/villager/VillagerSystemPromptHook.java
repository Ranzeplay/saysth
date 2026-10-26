package space.ranzeplay.saysth.villager;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

@FunctionalInterface
public interface VillagerSystemPromptHook {
    Optional<String> getAdditionalSystemPrompt(Villager villager, Player player, VillagerMemory memory);
}
