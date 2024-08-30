package space.ranzeplay.saysth.forge;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import space.ranzeplay.saysth.Main;

@Mod(Main.MOD_ID)
public final class MainForge {
    public static final Logger LOGGER = LogUtils.getLogger();

    public MainForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Main.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // final var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // modBus.addListener(ChatListener::onPlayerChat);
        MinecraftForge.EVENT_BUS.register(ChatListener.class);

        final var configDir = FMLPaths.CONFIGDIR.get();

        // Run our common setup.
        Main.init(configDir, LOGGER);
    }
}
