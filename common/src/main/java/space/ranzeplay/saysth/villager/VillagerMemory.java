package space.ranzeplay.saysth.villager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class VillagerMemory {
    UUID id;
    String name;
    String personality;
    String profession;
    String livingIn;
    HashMap<UUID, Conversation> conversations;

    public void addConversation(UUID playerId) {
        conversations.put(playerId, new Conversation(new ArrayList<>()));
    }

    public String getCharacter() {
        try {
            return Main.CONFIG_MANAGER.getSystemMessageTemplate()
                    .replace("{name}", name)
                    .replace("{personality}", personality.toLowerCase())
                    .replace("{profession}", profession.toLowerCase())
                    .replace("{livingIn}", livingIn.toLowerCase());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Conversation getConversation(UUID playerId) {
        if(!conversations.containsKey(playerId)) {
            conversations.put(playerId, new Conversation(new ArrayList<>()));
        }

        return conversations.get(playerId);
    }

    public void updateConversation(UUID playerId, Conversation conversation) {
        conversations.replace(playerId, conversation);
    }
}
