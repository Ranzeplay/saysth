package space.ranzeplay.saysth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.villager.ProfessionExtractor;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ConfigManager {
    private final Path configDirectoryPath;
    @Getter
    private SaySthConfig config;
    @Getter
    private IApiEndpointConfig apiConfig;
    @Getter
    private HashMap<String, String> professionSpecificPrompts;

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

    private Path getConclusionPromptTemplatePath() {
        return configDirectoryPath.resolve("conclusion-prompt-template.txt");
    }

    private Path getApiConfigFilePath() {
        return configDirectoryPath.resolve("api-config.json");
    }

    public void createConfigIfNotExists(ProfessionExtractor professionExtractor) throws IOException {
        if (!configDirectoryPath.toFile().exists()) {
            Main.LOGGER.info("Creating config directory");
            if (configDirectoryPath.toFile().mkdirs()) {
                Main.LOGGER.info("Config directory created");
            } else {
                Main.LOGGER.error("Failed to create config directory");
            }
        }

        if (!getConfigFilePath().toFile().exists()) {
            Main.LOGGER.info("Creating config file");
            if (getConfigFilePath().toFile().createNewFile()) {
                Main.LOGGER.info("Config file has been created");
            } else {
                Main.LOGGER.error("Config file already exists");
            }

            var defaultConfig = new SaySthConfig();
            var gson = new GsonBuilder().setPrettyPrinting().create();

            try (final var writer = new FileWriter(getConfigFilePath().toFile())) {
                writer.write(gson.toJson(defaultConfig, defaultConfig.getClass()));
            }
        }

        if (!getSystemMessageTemplatePath().toFile().exists()) {
            var stream = getClass().getResourceAsStream("/assets/villager-character-template.txt");
            assert stream != null;
            Files.copy(stream, getSystemMessageTemplatePath());

            stream.close();

            Main.LOGGER.info("System message template file has been created");
        }

        if (!getConclusionPromptTemplatePath().toFile().exists()) {
            var stream = getClass().getResourceAsStream("/assets/conclusion-prompt-template.txt");
            assert stream != null;
            Files.copy(stream, getConclusionPromptTemplatePath());

            stream.close();

            Main.LOGGER.info("Conclusion prompt template has been created");
        }

        if (!getProfessionPath().toFile().exists()) {
            Main.LOGGER.info("Creating profession directory");
            if (getProfessionPath().toFile().mkdirs()) {
                Main.LOGGER.info("Profession directory created");
            } else {
                Main.LOGGER.error("Failed to create profession directory");
            }
        }
        exportProfessions();

        if (!getVillagerMemoryPath().toFile().exists()) {
            Main.LOGGER.info("Creating villager memory directory");
            if (getVillagerMemoryPath().toFile().mkdirs()) {
                Main.LOGGER.info("Villager memory directory created");
            } else {
                Main.LOGGER.error("Failed to create villager memory directory");
            }
        }

        if (!getApiConfigFilePath().toFile().exists()) {
            Main.LOGGER.info("Creating API config file");
            if (getApiConfigFilePath().toFile().createNewFile()) {
                Main.LOGGER.info("API config file has been created");
            } else {
                Main.LOGGER.error("Failed to create API config file");
            }
            var defaultConfig = new CloudflareAIWorkerConfig();
            var gson = new GsonBuilder().setPrettyPrinting().create();

            try (final var writer = new FileWriter(getApiConfigFilePath().toFile())) {
                writer.write(gson.toJson(defaultConfig));
            }
        }
    }

    public void loadConfig() throws IOException {
        Main.LOGGER.info("Loading config");
        final var gson = new Gson();
        try (final var reader = new FileReader(getConfigFilePath().toFile())) {
            final var config = gson.fromJson(reader, SaySthConfig.class);
            if (config == null) {
                throw new IOException("Config file is empty or invalid");
            }
            this.config = config;
        }

        loadApiConfig();
        loadProfessionConfig();
    }

    public VillagerMemory getVillager(@NotNull UUID uuid) throws IOException {
        final var filePath = getVillagerMemoryPath().resolve(uuid + ".json").toFile();
        if (!filePath.exists() && !filePath.createNewFile()) {
            Main.LOGGER.warn("Failed to create memory for villager {}", uuid);
        }

        final var gson = new Gson();
        try (final var reader = new FileReader(filePath)) {
            final var villagerMemory = gson.fromJson(reader, VillagerMemory.class);
            if (villagerMemory == null) {
                throw new IOException("Villager memory file is empty or invalid for UUID: " + uuid);
            }
            return villagerMemory;
        }
    }

    public void updateVillager(@NotNull VillagerMemory villager) throws IOException {
        final var targetFile = getVillagerMemoryPath().resolve(villager.getId().toString() + ".json").toFile();
        final var gson = new GsonBuilder().setPrettyPrinting().create();
        try (final var writer = new FileWriter(targetFile, false)) {
            writer.write(gson.toJson(villager));
        }
    }

    public boolean isVillagerFileExists(@NotNull UUID uuid) {
        return getVillagerMemoryPath().resolve(uuid + ".json").toFile().exists();
    }

    public String getSystemMessageTemplate() throws IOException {
        return Files.readString(getSystemMessageTemplatePath());
    }

    public String getConclusionPromptTemplate() throws IOException {
        return Files.readString(getConclusionPromptTemplatePath());
    }

    private void loadApiConfig() throws IOException {
        var gson = new Gson();
        try (var reader = new FileReader(getApiConfigFilePath().toFile())) {
            switch (config.getApiConfigPlatform()) {
                case "openai-compatible" -> apiConfig = gson.fromJson(reader, OpenAICompatibleConfig.class);
                case "cloudflare" -> apiConfig = gson.fromJson(reader, CloudflareAIWorkerConfig.class);
                case "openai" -> apiConfig = gson.fromJson(reader, OpenAIConfig.class);
                default ->
                        throw new IllegalArgumentException("Invalid API config platform: " + config.getApiConfigPlatform());
            }

            if (apiConfig == null) {
                throw new IOException("API config file is empty or invalid");
            }
        }
    }

    private void loadProfessionConfig() throws IOException {
        professionSpecificPrompts = new HashMap<>();
        var professionDir = getProfessionPath().toFile();
        if (!professionDir.exists() || !professionDir.isDirectory()) {
            Main.LOGGER.warn("Profession directory does not exist: {}", professionDir.getPath());
            return;
        }

        var professionFiles = professionDir.listFiles(f -> f.getName().endsWith(".txt"));
        if (professionFiles == null || professionFiles.length == 0) {
            Main.LOGGER.warn("No profession configuration files found in: {}", professionDir.getPath());
            return;
        }

        for (var file : professionFiles) {
            if (file.isFile() && file.canRead()) {
                var text = Files.readString(file.toPath());
                professionSpecificPrompts.put(file.getName().replace(".txt", ""), text);
            }
        }
    }

    public void exportProfessions() {
        int count = 0;

        // Do not include leading slash for ZIP entry matching
        String resourceDirectory = "assets/professions";
        Path targetDir = getProfessionPath();

        try {
            // Dynamically locate the JAR file or class folder of this mod
            URL codeSource = ConfigManager.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeSource == null) return;

            boolean isDirectory = false;
            Path idePath = null;

            // Step 1: Detect if we are running from raw classes (IDE Mode)
            if ("file".equals(codeSource.getProtocol())) {
                Path p = Path.of(codeSource.toURI());
                if (Files.isDirectory(p)) {
                    isDirectory = true;
                    idePath = p.resolve(resourceDirectory);
                }
            }

            if (isDirectory) {
                // --- IDE Mode ---
                if (!Files.exists(idePath)) return;
                try (Stream<Path> walk = Files.walk(idePath)) {
                    Path finalIdePath = idePath;
                    walk.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".txt"))
                            .forEach(sourcePath -> {
                                try {
                                    Path relative = finalIdePath.relativize(sourcePath);
                                    Path target = targetDir.resolve(relative.toString()).normalize();
                                    if (target.startsWith(targetDir) && !Files.exists(target)) {
                                        Files.createDirectories(target.getParent());
                                        Files.copy(sourcePath, target);
                                    }
                                } catch (IOException e) {
                                    // Handle per-file logging here
                                }
                            });
                }
            } else {
                // --- Production Mode (Standalone JAR & Fabric Nested JiJ) ---
                // codeSource.openStream() reads the raw byte content of the container jar.
                // If nested, Java's URL handlers unpack the sub-jar bytes seamlessly into this stream.
                try (InputStream urlStream = codeSource.openStream();
                     ZipInputStream zipStream = new ZipInputStream(urlStream)) {

                    ZipEntry entry;
                    while ((entry = zipStream.getNextEntry()) != null) {
                        String entryName = entry.getName();

                        // "Browse" the directory by filtering entries that match the path criteria
                        if (entryName.startsWith(resourceDirectory + "/") && entryName.endsWith(".txt")) {

                            // Extract relative path inside assets folder
                            String relativePathStr = entryName.substring(resourceDirectory.length() + 1);
                            Path targetPath = targetDir.resolve(relativePathStr).normalize();

                            if (!Files.exists(targetPath)) {
                                Files.createDirectories(targetPath.getParent());

                                // Files.copy streams bytes until the current ZipEntry's EOF,
                                // automatically leaving the underlying zipStream open for the next loop.
                                Files.copy(zipStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                                count++;
                            }
                        }

                        zipStream.closeEntry();
                    }
                }
            }
        } catch (Exception e) {
            Main.LOGGER.warn("Failed to export professions from {}", resourceDirectory, e);
        }

        Main.LOGGER.info("Exported {} professions", count);
    }
}
