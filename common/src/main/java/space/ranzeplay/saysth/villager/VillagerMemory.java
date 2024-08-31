package space.ranzeplay.saysth.villager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import space.ranzeplay.saysth.chat.Conversation;

import java.util.HashMap;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class VillagerMemory {
    UUID id;
    String name;
    String personality;
    HashMap<String, Conversation> conversations;
}
