package space.ranzeplay.saysth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ConfigManager {
    private final Path configDirectoryPath;
    @Getter
    private SaySthConfig config;

    public ConfigManager(@NotNull Path configDirectoryPath) {
        this.configDirectoryPath = configDirectoryPath;
    }

    private Path getConfigFilePath() {
        return configDirectoryPath.resolve("saysth-config.json");
    }

    private Path getVillagerMemoryPath() {
        return configDirectoryPath.resolve("saysth-villagers");
    }

    private Path getSystemMessageTemplatePath() {
        return configDirectoryPath.resolve("saysth-sys-msg-template.txt");
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

        if(!getSystemMessageTemplatePath().toFile().exists()) {
            Files.createFile(getSystemMessageTemplatePath());
            Files.writeString(getSystemMessageTemplatePath(),
                    """
                    You are going to play a Minecraft villager whose name is {name}.
                    You are a {personality} guy.
                    Your profession is {profession}.
                    You live in {livingIn}
                    You use emeralds as currency.
                    You should speak the same language as the other said
                    You will response "IGN" but nothing else even a single character if user speaks to someone other than you.
                    You should speak concisely since you cannot speak too much at once.
                    You tend to know the other's name first when conversation starts.
                    """);
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

    public VillagerMemory getVillager(@NotNull UUID uuid) throws IOException {
        final var filePath = getVillagerMemoryPath().resolve(uuid + ".json").toFile();
        filePath.createNewFile();

        final var reader = new FileReader(filePath);
        final var gson = new Gson();
        final var config = gson.fromJson(reader, VillagerMemory.class);
        reader.close();

        return config;
    }

    public void updateVillager(@NotNull VillagerMemory villager) throws IOException {
        final var targetFile = getVillagerMemoryPath().resolve(villager.getId().toString() + ".json").toFile();
        final var writer = new FileWriter(targetFile);
        var gson = new GsonBuilder().setPrettyPrinting().create();
        writer.write(gson.toJson(villager));
        writer.close();
    }

    public boolean isVillagerFileExists(@NotNull UUID uuid) {
        return getVillagerMemoryPath().resolve(uuid + ".json").toFile().exists();
    }

    public String getSystemMessageTemplate() throws IOException {
        return Files.readString(getSystemMessageTemplatePath());
    }
}
