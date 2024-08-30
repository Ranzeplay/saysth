package space.ranzeplay.saysth.forge;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.events.PlayerChatEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatListener {
    @SubscribeEvent
    public static void onPlayerChat(final ServerChatEvent event) throws IOException {
        MainForge.LOGGER.info("Player chat");
        PlayerChatEvent.onPlayerChat(event.getPlayer(), event.getMessage().getString());
    }
}
