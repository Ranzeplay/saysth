package space.ranzeplay.saysth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class ConfigManager {
    private final Path configDirectoryPath;
    @Getter
    private SaySthConfig config;
    @Getter
    private HashMap<UUID, VillagerMemory> villagers;

    public ConfigManager(Path configDirectoryPath) {
        this.configDirectoryPath = configDirectoryPath;
    }

    private Path getConfigFilePath() {
        return configDirectoryPath.resolve("saysth-config.json");
    }

    private Path getVillagerMemoryPath() {
        return configDirectoryPath.resolve("saysth-villagers.json");
    }

    public void createConfigIfNotExists() throws IOException {
        if(!configDirectoryPath.toFile().exists()) {
            Main.LOGGER.info("Creating config directory");
            configDirectoryPath.toFile().mkdirs();
        }

        if(!getConfigFilePath().toFile().exists()) {
            Main.LOGGER.info("Creating config file");
            getConfigFilePath().toFile().createNewFile();

            var defaultConfig = new SaySthConfig();
            var gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(defaultConfig, defaultConfig.getClass());

            final var writer = new FileWriter(getConfigFilePath().toFile());
            writer.write(gson.toJson(defaultConfig));
            writer.close();
        }

        if(!getVillagerMemoryPath().toFile().exists()) {
            Main.LOGGER.info("Creating villager memory file");
            getVillagerMemoryPath().toFile().createNewFile();

            var defaultConfig = new HashMap<Integer, VillagerMemory>();
            var gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(defaultConfig, defaultConfig.getClass());

            final var writer = new FileWriter(getVillagerMemoryPath().toFile());
            writer.write(gson.toJson(defaultConfig));
            writer.close();
        }
    }

    public void loadConfig() throws IOException {
        Main.LOGGER.info("Loading config");
        final var reader = new FileReader(getConfigFilePath().toFile());
        final var gson = new Gson();
        final var config = gson.fromJson(reader, SaySthConfig.class);
        reader.close();
        this.config = config;
    }

    public void loadVillagers() throws IOException {
        Main.LOGGER.info("Loading villagers");
        final var reader = new FileReader(getVillagerMemoryPath().toFile());
        final var gson = new Gson();
        final var villagers = gson.fromJson(reader, HashMap.class);
        reader.close();
        this.villagers = villagers;
    }

    public void setVillagers(HashMap<UUID, VillagerMemory> villagers) throws IOException {
        this.villagers = villagers;

        var gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(villagers, villagers.getClass());

        final var writer = new FileWriter(getVillagerMemoryPath().toFile());
        writer.write(gson.toJson(villagers));
        writer.close();
    }
}
