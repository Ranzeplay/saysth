package space.ranzeplay.saysth.villager;

import com.google.gson.Gson;
import net.minecraft.world.entity.npc.Villager;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Llama3Response;
import space.ranzeplay.saysth.chat.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class VillagerManager {
    private static VillagerMemory generateRandomVillagerMemory(Villager villager) {
        final var random = new Random();
        final var nameCandidates = Main.CONFIG_MANAGER.getConfig().getNameCandidates();
        final var personalityCandidates = Main.CONFIG_MANAGER.getConfig().getPersonalities();

        final var newName = nameCandidates[random.nextInt(0, nameCandidates.length)];
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
        if (!Main.CONFIG_MANAGER.isVillagerFileExists(villager.getUUID())) {
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

    public String sendMessageToVillager(UUID villagerId, UUID playerId, String message) throws IOException, InterruptedException {
        final var memory = Main.CONFIG_MANAGER.getVillager(villagerId);
        if (!memory.conversations.containsKey(playerId)) {
            memory.addConversation(playerId);
        }
        final var conversation = memory.getConversation(playerId);
        conversation.addMessage(new Message(ChatRole.USER, message));

        final var bodyText = new Gson().toJson(conversation);
        Main.LOGGER.info(bodyText);

        // Send web request
        var request = HttpRequest.newBuilder(URI.create("https://api.cloudflare.com/client/v4/accounts/" + Main.CONFIG_MANAGER.getConfig().getCloudflareUserId() + "/ai/run/@cf/meta/llama-3-8b-instruct"))
                .POST(HttpRequest.BodyPublishers.ofString(bodyText))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .headers("Authorization", "Bearer " + Main.CONFIG_MANAGER.getConfig().getCloudflareApiKey())
                .build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        var text = new Gson().fromJson(response.body(), Llama3Response.class).getResult().getResponse();
        conversation.addMessage(new Message(ChatRole.ASSISTANT, text));
        memory.updateConversation(playerId, conversation);

        this.updateVillager(memory);

        return text;
    }
}
