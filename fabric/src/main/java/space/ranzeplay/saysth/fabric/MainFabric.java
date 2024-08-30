package space.ranzeplay.saysth.fabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import space.ranzeplay.saysth.Main;
import net.fabricmc.api.ModInitializer;

public final class MainFabric implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        final var configDir = FabricLoader.getInstance().getConfigDir();
        Main.init(configDir, LOGGER);
    }
}
