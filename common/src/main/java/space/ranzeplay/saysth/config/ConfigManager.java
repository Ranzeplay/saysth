package space.ranzeplay.saysth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.io.*;
import java.nio.file.Path;
import java.util.UUID;

public class ConfigManager {
    private final Path configDirectoryPath;
    @Getter
    private SaySthConfig config;

    public ConfigManager(Path configDirectoryPath) {
        this.configDirectoryPath = configDirectoryPath;
    }

    private Path getConfigFilePath() {
        return configDirectoryPath.resolve("saysth-config.json");
    }

    private Path getVillagerMemoryPath() {
        return configDirectoryPath.resolve("saysth-villagers");
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
            Main.LOGGER.info("Creating villager memory directory");
            getVillagerMemoryPath().toFile().mkdirs();
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

    public VillagerMemory getVillager(UUID uuid) throws IOException {
        final var filePath = getVillagerMemoryPath().resolve(uuid.toString() + ".json").toFile();
        if(!filePath.exists()) {
            filePath.createNewFile();
        }

        final var reader = new FileReader(filePath);
        final var gson = new Gson();
        final var config = gson.fromJson(reader, VillagerMemory.class);
        reader.close();

        return config;
    }

    public void updateVillager(VillagerMemory villager) throws IOException {
        final var targetFile = getVillagerMemoryPath().resolve(villager.getId().toString() + ".json").toFile();
        final var writer = new FileWriter(targetFile);
        var gson = new GsonBuilder().setPrettyPrinting().create();
        writer.write(gson.toJson(villager));
        writer.close();
    }

    public boolean isVillagerFileExists(UUID uuid) {
        return getVillagerMemoryPath().resolve(uuid.toString() + ".json").toFile().exists();
    }
}
