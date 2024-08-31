package space.ranzeplay.saysth;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.slf4j.Logger;
import space.ranzeplay.saysth.config.ConfigManager;
import space.ranzeplay.saysth.villager.VillagerManager;

import java.io.IOException;
import java.nio.file.Path;

public final class Main {
    public static final String MOD_ID = "saysth";
    public static ConfigManager CONFIG_MANAGER;
    public static VillagerManager VILLAGER_MANAGER;
    public static Logger LOGGER;

    public static LanguageDetector LANGUAGE_DETECTOR;

    public static void init(Path configDir, Logger logger){
        CONFIG_MANAGER = new ConfigManager(configDir);
        LOGGER = logger;
        try {
            CONFIG_MANAGER.createConfigIfNotExists();
            CONFIG_MANAGER.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VILLAGER_MANAGER = new VillagerManager();

        LANGUAGE_DETECTOR = LanguageDetectorBuilder.fromLanguages(Language.CHINESE, Language.ENGLISH, Language.JAPANESE).build();
    }
}
