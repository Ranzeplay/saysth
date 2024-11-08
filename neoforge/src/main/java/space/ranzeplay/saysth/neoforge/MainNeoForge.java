package space.ranzeplay.saysth.neoforge;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import space.ranzeplay.saysth.Main;

import static space.ranzeplay.saysth.Main.MOD_ID;

@Mod(MOD_ID)
public final class MainNeoForge {
    public static final Logger LOGGER = LogUtils.getLogger();

    public MainNeoForge() {
        NeoForge.EVENT_BUS.register(new ChatListener());

        final var configDir = FMLPaths.CONFIGDIR.get();

        // Run our common setup.
        Main.init(configDir, LOGGER);
    }
}
