package space.ranzeplay.saysth.fabric;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import space.ranzeplay.saysth.events.PlayerChatEvent;

import java.io.IOException;

public class ChatMessageListener implements ServerMessageEvents.ChatMessage {
    @Override
    public void onChatMessage(PlayerChatMessage playerChatMessage, ServerPlayer serverPlayer, ChatType.Bound bound) {
        try {
            PlayerChatEvent.onPlayerChat(serverPlayer, playerChatMessage.signedContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
