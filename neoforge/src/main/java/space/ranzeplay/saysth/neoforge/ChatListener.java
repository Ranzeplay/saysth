package space.ranzeplay.saysth.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import space.ranzeplay.saysth.events.PlayerChatEvent;

import java.io.IOException;

public class ChatListener {
    @SubscribeEvent
    public void onPlayerChat(final ServerChatEvent event) throws IOException {
        if (event == null || event.getPlayer() == null || event.getMessage() == null) {
            MainNeoForge.LOGGER.warn("Null event or player in chat listener");
            return;
        }
        
        MainNeoForge.LOGGER.info("Player chat");
        PlayerChatEvent.onPlayerChat(event.getPlayer(), event.getMessage().getString());
    }
}
