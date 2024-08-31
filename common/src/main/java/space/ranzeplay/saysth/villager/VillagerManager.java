package space.ranzeplay.saysth.villager;

import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.NotNull;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.config.ConfigManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class VillagerManager {
    private final HashMap<UUID, VillagerMemory> villagers;

    public VillagerManager(ConfigManager cfg) {
        this.villagers = cfg.getVillagers();
    }

    public Optional<VillagerMemory> addNewVillager(Villager villager, boolean resetIfExists) {
        final var memory = generateRandomVillagerMemory(villager);

        if (!resetIfExists && villagers.containsKey(villager.getUUID())) {
            return Optional.empty();
        }

        if(resetIfExists) {
            villagers.remove(villager.getUUID());
        }
        villagers.put(villager.getUUID(), memory);

        try {
            Main.CONFIG_MANAGER.setVillagers(villagers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.of(memory);
    }

    private static VillagerMemory generateRandomVillagerMemory(Villager villager) {
        final var random = new Random();
        final var nameCandidates = Main.CONFIG_MANAGER.getConfig().getNameCandidates();
        final var personalityCandidates = Main.CONFIG_MANAGER.getConfig().getPersonalities();

        final var newName = nameCandidates[random.nextInt(0, nameCandidates.length)];
        final var newPersonality =  personalityCandidates[random.nextInt(0, personalityCandidates.length)];

        final var memory = new VillagerMemory(villager.getUUID(), newName, newPersonality, new HashMap<>());
        return memory;
    }

    public VillagerMemory getById(UUID uuid) {
        return villagers.get(uuid);
    }

    public boolean isVillagerConfigured(UUID uuid) {
        return villagers.containsKey(uuid);
    }
}
