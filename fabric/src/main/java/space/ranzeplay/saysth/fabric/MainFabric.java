package space.ranzeplay.saysth.fabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import space.ranzeplay.saysth.Main;
import net.fabricmc.api.ModInitializer;

public final class MainFabric implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        final var configDir = FabricLoader.getInstance().getConfigDir();
        Main.init(configDir, LOGGER);

        ServerMessageEvents.CHAT_MESSAGE.register(new ChatMessageListener());
    }
}
