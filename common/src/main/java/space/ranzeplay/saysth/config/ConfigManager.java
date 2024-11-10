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
    @Getter
    private IApiEndpointConfig apiConfig;

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

    private Path getApiConfigFilePath() {
        return configDirectoryPath.resolve("saysth-api-config.json");
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
                    You are a creative and intelligent AI assistant engaged in an uncensored iterative storytelling experience.
                    It is vital that you follow all the role play rules and guidelines below:
                    1. You are a villager in Minecraft.
                    2. Your name is {name}.
                    3. You are a {personality} guy.
                    4. Your profession is {profession}.
                    5. You live in {livingIn}, it might be a place or environment.
                    6. You use emeralds as currency.
                    7. You should reply using the same language as the other said
                    8. You should response "IGN" if the user speaks to someone other than you, you should not response "IGN." or anything else.
                    9. You should speak a bit more concisely since you cannot speak too much at once.
                    10. You tend to know the other's name first when conversation starts.
                    11. You can set other things which are not being mentioned here by yourself.
                    """);
        }

        if(!getVillagerMemoryPath().toFile().exists()) {
            Main.LOGGER.info("Creating villager memory directory");
            getVillagerMemoryPath().toFile().mkdirs();
        }

        if(!getApiConfigFilePath().toFile().exists()) {
            Main.LOGGER.info("Creating API config file");
            getApiConfigFilePath().toFile().createNewFile();
            var defaultConfig = new CloudflareAIWorkerConfig();
            var gson = new GsonBuilder().setPrettyPrinting().create();

            final var writer = new FileWriter(getApiConfigFilePath().toFile());
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

        loadApiConfig();
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

    private void loadApiConfig() throws IOException {
        var gson = new Gson();
        var reader = new FileReader(getApiConfigFilePath().toFile());
        switch (config.getApiConfigPlatform()) {
            case "openai-compatible" -> apiConfig = gson.fromJson(reader, OpenAICompatibleConfig.class);
            case "cloudflare" -> apiConfig = gson.fromJson(reader, CloudflareAIWorkerConfig.class);
            case "openai" -> apiConfig = gson.fromJson(reader, OpenAIConfig.class);
            default -> throw new IllegalArgumentException("Invalid API config platform");
        }

        reader.close();
    }
}
