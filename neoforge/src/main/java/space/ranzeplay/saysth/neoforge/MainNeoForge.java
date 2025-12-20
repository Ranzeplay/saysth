package space.ranzeplay.saysth.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.neoforge.debug.ConsoleDebugCommands;

import static space.ranzeplay.saysth.Main.MOD_ID;

@Mod(MOD_ID)
public final class MainNeoForge {
    public static final Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);;

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
