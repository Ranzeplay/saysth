package space.ranzeplay.saysth.neoforge;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.neoforge.debug.ConsoleDebugCommands;

import static space.ranzeplay.saysth.Main.MOD_ID;

@Mod(MOD_ID)
public final class MainNeoForge {
    public static final Logger LOGGER = LogUtils.getLogger();

    public MainNeoForge() {
        NeoForge.EVENT_BUS.register(new ChatListener());
        NeoForge.EVENT_BUS.register(this);

        final var configDir = FMLPaths.CONFIGDIR.get();

        // Run our common setup.
        Main.init(configDir, LOGGER);
        
        // Initialize console debug handler
        ConsoleDebugCommands.initialize();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ConsoleDebugCommands.register(event.getDispatcher());
    }
}
