package space.ranzeplay.saysth;

import org.slf4j.Logger;
import space.ranzeplay.saysth.config.ConfigManager;
import space.ranzeplay.saysth.debug.ConsoleConversationHandler;
import space.ranzeplay.saysth.villager.VillagerManager;

import java.nio.file.Path;

public final class Main {
    public static final String MOD_ID = "saysth";

    public static ConfigManager CONFIG_MANAGER;
    public static VillagerManager VILLAGER_MANAGER;
    public static ConsoleConversationHandler CONSOLE_HANDLER;

    public static Logger LOGGER;

    public static void init(Path configDir, Logger logger){
        if (logger == null) {
            throw new IllegalArgumentException("Logger cannot be null");
        }
        if (configDir == null) {
            throw new IllegalArgumentException("Config directory cannot be null");
        }
        
        LOGGER = logger;

        CONFIG_MANAGER = new ConfigManager(configDir);
        try {
            CONFIG_MANAGER.createConfigIfNotExists();
            CONFIG_MANAGER.loadConfig();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize configuration", e);
        }

        VILLAGER_MANAGER = new VillagerManager();
        CONSOLE_HANDLER = new ConsoleConversationHandler();
    }
}
