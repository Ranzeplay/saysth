package space.ranzeplay.saysth.villager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;

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
        return String.format("""
                        You are going to play a Minecraft villager whose name is %s.
                        You are a %s guy.
                        Your profession is %s.
                        You live in %s
                        You use emeralds as currency.
                        You will response "IGN" but nothing else even a single character if user speaks to someone other than you.
                        You should speak concisely since you cannot speak too much at once.
                        You tend to know the other's name first when conversation starts.
                        """,
                this.name, this.personality, this.profession, this.livingIn);
    }

    public Conversation getConversation(UUID playerId) {
        if(!conversations.containsKey(playerId)) {
            conversations.put(playerId, new Conversation(new ArrayList<>()));
        }

        var conversation = conversations.get(playerId);
        if(conversation.messages.isEmpty()) {
            conversation.addMessage(new Message(ChatRole.SYSTEM, getCharacter()));
        }

        return conversation;
    }

    public void updateConversation(UUID playerId, Conversation conversation) {
        conversations.replace(playerId, conversation);
    }
}
