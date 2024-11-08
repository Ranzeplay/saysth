package space.ranzeplay.saysth.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import space.ranzeplay.saysth.events.PlayerChatEvent;

import java.io.IOException;

public class ChatListener {
    @SubscribeEvent
    public void onPlayerChat(final ServerChatEvent event) throws IOException {
        MainNeoForge.LOGGER.info("Player chat");
        PlayerChatEvent.onPlayerChat(event.getPlayer(), event.getMessage().getString());
    }
}
