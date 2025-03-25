package space.ranzeplay.saysth.villager;

import com.google.gson.Gson;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;
import space.ranzeplay.saysth.config.ConfigManager;
import space.ranzeplay.saysth.instant.InstantDataProvider;

import java.io.IOException;
import java.util.*;

public class VillagerManager {
    private static final String LLM_CONCLUDE_PROMPT = "Conclude the following paragraphs into one complete paragraph concisely using English.";

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

    public Optional<String> sendMessageToVillager(Villager villager, Player player, String message) throws IOException {
        final var instantDataProvider = new InstantDataProvider(villager, player);

        var memory = Main.CONFIG_MANAGER.getVillager(villager.getUUID());
        if (!memory.conversations.containsKey(player.getUUID())) {
            memory.addConversation(player.getUUID());
        }
        final var conversation = memory.getConversation(player.getUUID());
        conversation.addMessage(new Message(ChatRole.USER, message));

        // Push system messages including villager's trades and character description
        // Villager character will be on the top of the conversation
        // Villager trades will be the second message
        conversation.messages.addFirst(new Message(ChatRole.SYSTEM, formatVillagerTrades(instantDataProvider.getOffers())));
        conversation.messages.addFirst(new Message(ChatRole.SYSTEM, memory.getCharacter()));

        final var promptMap = Main.CONFIG_MANAGER.getProfessionSpecificPrompts();
        if(promptMap.keySet().stream().anyMatch(p -> p.equalsIgnoreCase(villager.getVillagerData().getProfession().name()))) {
            conversation.addMessage(new Message(ChatRole.SYSTEM, promptMap.get(villager.getVillagerData().getProfession().name())));
        }

        conversation.messages.addFirst(new Message(ChatRole.SYSTEM, formatPlayerEffects(instantDataProvider.getPlayerEffects())));

        final var response = Main.CONFIG_MANAGER.getApiConfig().sendConversationAndGetResponseText(conversation);
        VillagerMemory finalMemory = memory;
        response.ifPresent(m -> conversation.addMessage(new Message(ChatRole.ASSISTANT, m)));

        // Remove system messages
        conversation.messages.removeFirst();
        conversation.messages.removeFirst();

        finalMemory.updateConversation(player.getUUID(), conversation);

        // Conclude memory if it's going to too large
        if (conversation.messages.size() > Main.CONFIG_MANAGER.getConfig().getConclusionMessageLimit()) {
            memory = concludeMemory(memory, player.getUUID());
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
        final var conclusion = Main.CONFIG_MANAGER.getApiConfig().sendConversationAndGetResponseText(model);
        conclusion.ifPresent(m -> {
            var conversation = new Conversation(new ArrayList<>());
            conversation.addMessage(new Message(ChatRole.SYSTEM, m));
            villager.updateConversation(playerId, conversation);
        });

        return villager;
    }

    public String formatVillagerTrades(MerchantOffers trades) {
        if (trades.isEmpty()) {
            return "You don't sell anything for now.";
        }

        var formattedTrades = new ArrayList<String>();
        formattedTrades.add("You ONLY have the following trades:");

        for (var trade : trades) {
            StringBuilder formattedTrade = new StringBuilder();

            formattedTrade.append("- ")
                    .append(trade.getCostA().getCount())
                    .append("x ")
                    .append(trade.getCostA().getHoverName().getString().toLowerCase());

            if (!trade.getCostB().isEmpty()) {
                formattedTrade.append(" and ")
                        .append(trade.getCostB().getCount())
                        .append("x ")
                        .append(trade.getCostB().getHoverName().getString().toLowerCase());
            }

            formattedTrade.append(" for ")
                    .append(trade.getResult().getCount())
                    .append("x ")
                    .append(trade.getResult().getHoverName().getString().toLowerCase());

            formattedTrades.add(formattedTrade.toString());
        }
        return String.join("\n", formattedTrades);
    }

    public String formatPlayerEffects(MobEffectInstance[] effects) {
        if (effects.length == 0) {
            return "The player does not have any effects.";
        }

        var formattedEffects = new ArrayList<String>();
        formattedEffects.add("The player has the following effects:");

        for (var effect : effects) {
            String formattedEffect = "- " +
                    effect.getEffect().getRegisteredName() +
                    " for " +
                    effect.getDuration() / 20 +
                    " seconds";

            formattedEffects.add(formattedEffect);
        }
        return String.join("\n", formattedEffects);
    }
}
