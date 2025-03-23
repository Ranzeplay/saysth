package space.ranzeplay.saysth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.jar.JarFile;

public class ConfigManager {
    private final Path configDirectoryPath;
    @Getter
    private SaySthConfig config;
    @Getter
    private IApiEndpointConfig apiConfig;

    public ConfigManager(@NotNull Path minecraftConfigDirectoryPath) {
        this.configDirectoryPath = minecraftConfigDirectoryPath.resolve("saysth");
    }

    private Path getConfigFilePath() {
        return configDirectoryPath.resolve("config.json");
    }

    private Path getVillagerMemoryPath() {
        return configDirectoryPath.resolve("villagers");
    }

    private Path getProfessionPath() {
        return configDirectoryPath.resolve("professions");
    }

    private Path getSystemMessageTemplatePath() {
        return configDirectoryPath.resolve("villager-character-template.txt");
    }

    private Path getApiConfigFilePath() {
        return configDirectoryPath.resolve("api-config.json");
    }

    public void createConfigIfNotExists() throws IOException, URISyntaxException {
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
            var stream = getClass().getResourceAsStream("/assets/villager-character-template.txt");
            // Files.createFile(getSystemMessageTemplatePath());
            assert stream != null;
            Files.copy(stream, getSystemMessageTemplatePath());

            stream.close();
        }

        if(!getProfessionPath().toFile().exists()) {
            Main.LOGGER.info("Creating profession directory");
            getProfessionPath().toFile().mkdirs();
        }
        var professions = getClass().getResource("/assets/professions");
        assert professions != null;
        var entries = new JarFile(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())).entries();
        while (entries.hasMoreElements()) {
            var entry = entries.nextElement();
            if (entry.getName().startsWith("assets/professions/") && entry.getName().endsWith(".txt")) {
                var targetFile = getProfessionPath().resolve(entry.getName().substring("assets/professions/".length())).toFile();
                if (!targetFile.exists()) {
                    // targetFile.createNewFile();
                    var stream = getClass().getResourceAsStream("/" + entry.getName());
                    assert stream != null;
                    Files.copy(stream, targetFile.toPath());
                    stream.close();
                }
            }
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
