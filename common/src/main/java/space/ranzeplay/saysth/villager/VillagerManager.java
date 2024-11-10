package space.ranzeplay.saysth.villager;

import com.google.gson.Gson;
import net.minecraft.world.entity.npc.Villager;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.ChatResponse;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.*;

public class VillagerManager {
    private static final String LLM_CONCLUDE_PROMPT = "I'll give you a json text containing a conversation, you need to conclude the content into a complete paragraph concisely using English.";

    private static VillagerMemory generateRandomVillagerMemory(Villager villager) {
        final var random = new Random();
        final var nameCandidates = Main.CONFIG_MANAGER.getConfig().getNameCandidates();
        final var personalityCandidates = Main.CONFIG_MANAGER.getConfig().getPersonalities();

        String newName;
        if (villager.getCustomName() != null && Main.CONFIG_MANAGER.getConfig().isUseExistingVillagerName()) {
            newName = villager.getCustomName().getString();
        } else {
            newName = nameCandidates[random.nextInt(0, nameCandidates.length)];
        }

        final var newPersonality = personalityCandidates[random.nextInt(0, personalityCandidates.length)];

        return new VillagerMemory(villager.getUUID(),
                newName,
                newPersonality,
                villager.getVillagerData().getProfession().name(),
                villager.getVillagerData().getType().toString(),
                new HashMap<>());
    }

    public VillagerMemory getVillager(Villager villager) throws IOException {
        final VillagerMemory memory;
        if (!hasVillager(villager.getUUID())) {
            memory = generateRandomVillagerMemory(villager);
            Main.CONFIG_MANAGER.updateVillager(memory);
        } else {
            memory = Main.CONFIG_MANAGER.getVillager(villager.getUUID());
        }

        return memory;
    }

    public void updateVillager(VillagerMemory memory) throws IOException {
        Main.CONFIG_MANAGER.updateVillager(memory);
    }

    public Optional<String> sendMessageToVillager(UUID villagerId, UUID playerId, String message) throws IOException {
        var memory = Main.CONFIG_MANAGER.getVillager(villagerId);
        if (!memory.conversations.containsKey(playerId)) {
            memory.addConversation(playerId);
        }
        final var conversation = memory.getConversation(playerId);
        conversation.addMessage(new Message(ChatRole.USER, message));

        var response = sendConversationToCloudflareLLM(conversation);
        VillagerMemory finalMemory = memory;
        response.ifPresent(m -> {
            conversation.addMessage(new Message(ChatRole.ASSISTANT, m));
            finalMemory.updateConversation(playerId, conversation);
        });

        // Conclude memory if it's going to too large
        if (conversation.messages.size() > Main.CONFIG_MANAGER.getConfig().getConclusionMessageLimit()) {
            memory = concludeMemory(memory, playerId);
        }

        this.updateVillager(memory);

        return response;
    }

    public boolean hasVillager(UUID villagerId) {
        return Main.CONFIG_MANAGER.isVillagerFileExists(villagerId);
    }

    public VillagerMemory concludeMemory(VillagerMemory villager, UUID playerId) {
        var gson = new Gson();
        var model = new Conversation(new ArrayList<>());
        model.addMessage(new Message(ChatRole.SYSTEM, LLM_CONCLUDE_PROMPT));
        model.addMessage(new Message(ChatRole.USER, gson.toJson(villager.getConversation(playerId))));
        final var villagerSystemPrompt = villager.getConversation(playerId).messages.getFirst();
        final var conclusion = sendConversationToCloudflareLLM(model);
        conclusion.ifPresent(m -> {
            var conversation = new Conversation(new ArrayList<>());
            conversation.addMessage(villagerSystemPrompt);
            conversation.addMessage(new Message(ChatRole.SYSTEM, m));
            villager.updateConversation(playerId, conversation);
        });

        return villager;
    }

    private Optional<String> sendConversationToCloudflareLLM(Conversation conversation) {
        final var gson = new Gson();
        final var config = Main.CONFIG_MANAGER.getConfig();
        final var request = HttpRequest.newBuilder(URI.create(config.getApiEndpointUrl()))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(conversation)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .headers("Authorization", config.getAuthCredentials())
                .build();
        final HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Main.LOGGER.warn(e.getMessage());
            return Optional.empty();
        }
        return Optional.of(gson.fromJson(response.body(), ChatResponse.class).getResult().getResponse());
    }
}
