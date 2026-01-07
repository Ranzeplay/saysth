package space.ranzeplay.saysth.villager;

import com.google.gson.Gson;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.ChatRole;
import space.ranzeplay.saysth.chat.Conversation;
import space.ranzeplay.saysth.chat.Message;

import java.io.IOException;
import java.util.*;

public class VillagerManager {
    private static VillagerMemory generateRandomVillagerMemory(Villager villager) {
        if (villager == null) {
            throw new IllegalArgumentException("Villager cannot be null");
        }
        
        final var random = new Random();
        final var nameCandidates = Main.CONFIG_MANAGER.getConfig().getNameCandidates();
        final var personalityCandidates = Main.CONFIG_MANAGER.getConfig().getPersonalities();

        String newName;
        if (villager.getCustomName() != null && Main.CONFIG_MANAGER.getConfig().isUseExistingVillagerName()) {
            newName = villager.getCustomName().getString();
        } else {
            if (nameCandidates == null || nameCandidates.length == 0) {
                newName = "Villager";
                Main.LOGGER.warn("No name candidates found in config, using default name");
            } else {
                newName = nameCandidates[random.nextInt(nameCandidates.length)];
            }
        }

        String newPersonality;
        if (personalityCandidates == null || personalityCandidates.length == 0) {
            newPersonality = "friendly";
            Main.LOGGER.warn("No personality candidates found in config, using default personality");
        } else {
            newPersonality = personalityCandidates[random.nextInt(personalityCandidates.length)];
        }

        return new VillagerMemory(villager.getUUID(),
                newName,
                newPersonality,
                villager.getVillagerData().profession().value().name().getString(),
                villager.getVillagerData().type().getRegisteredName(),
                new HashMap<>());
    }

    public VillagerMemory getVillager(Villager villager) throws IOException {
        if (villager == null) {
            throw new IllegalArgumentException("Villager cannot be null");
        }
        
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
        if (villager == null || player == null) {
            throw new IllegalArgumentException("Villager and player cannot be null");
        }
        if (message == null || message.trim().isEmpty()) {
            Main.LOGGER.warn("Received empty message, ignoring");
            return Optional.empty();
        }
        
        var memory = getOrCreateVillagerMemory(villager, player);
        final var conversation = memory.getConversation(player.getUUID());
        conversation.addMessage(new Message(ChatRole.USER, message));

        Main.LOGGER.debug("Conversation with villager (before post) {}: {}", villager.getUUID(), conversation);

        int systemMessagesAdded = addSystemMessagesToConversation(conversation, villager, memory);
        final var response = Main.CONFIG_MANAGER.getApiConfig().sendConversationAndGetResponseText(conversation);
        response.ifPresent(m -> conversation.addMessage(new Message(ChatRole.ASSISTANT, m)));

        removeSystemMessages(conversation, systemMessagesAdded);
        Main.LOGGER.debug("Conversation with villager (after post) {}: {}", villager.getUUID(), conversation);

        memory.updateConversation(player.getUUID(), conversation);
        memory = concludeMemoryIfNeeded(memory, player.getUUID(), conversation);
        this.updateVillager(memory);

        return response;
    }

    private VillagerMemory getOrCreateVillagerMemory(Villager villager, Player player) throws IOException {
        var memory = Main.CONFIG_MANAGER.getVillager(villager.getUUID());
        if (!memory.conversations.containsKey(player.getUUID())) {
            memory.addConversation(player.getUUID());
        }
        return memory;
    }

    private int addSystemMessagesToConversation(Conversation conversation, Villager villager, VillagerMemory memory) {
        // Push system messages including villager's trades and character description
        // Order: character (first), profession-specific prompt (optional), trades
        conversation.messages.addFirst(new Message(ChatRole.SYSTEM, formatVillagerTrades(villager)));
        int systemMessagesAdded = 1;

        String professionPrompt = getProfessionSpecificPrompt(villager);
        if (professionPrompt != null && !professionPrompt.isBlank()) {
            conversation.messages.addFirst(new Message(ChatRole.SYSTEM, professionPrompt));
            systemMessagesAdded++;
        }

        conversation.messages.addFirst(new Message(ChatRole.SYSTEM, memory.getCharacter()));
        systemMessagesAdded++;

        return systemMessagesAdded;
    }

    private String getProfessionSpecificPrompt(Villager villager) {
        final var promptMap = Main.CONFIG_MANAGER.getProfessionSpecificPrompts();
        final var profession = villager.getVillagerData().profession().value().name().getString();
        
        String matchedKey = promptMap.keySet().stream()
                .filter(p -> p.equalsIgnoreCase(profession))
                .findFirst()
                .orElse(null);
        
        return matchedKey != null ? promptMap.get(matchedKey) : null;
    }

    private void removeSystemMessages(Conversation conversation, int systemMessagesToRemove) {
        // Remove system messages that were temporarily added
        for (int i = 0; i < systemMessagesToRemove && !conversation.messages.isEmpty(); i++) {
            if (conversation.messages.getFirst().getRole() == ChatRole.SYSTEM) {
                conversation.messages.removeFirst();
            } else {
                break; // Stop if we encounter a non-system message
            }
        }
    }

    private VillagerMemory concludeMemoryIfNeeded(VillagerMemory memory, UUID playerId, Conversation conversation) throws IOException {
        final var messageLimit = Main.CONFIG_MANAGER.getConfig().getConclusionMessageLimit();
        if (conversation.messages.size() > messageLimit && messageLimit > 0) {
            return concludeMemory(memory, playerId);
        }
        return memory;
    }

    public boolean hasVillager(UUID villagerId) {
        if (villagerId == null) {
            Main.LOGGER.warn("Attempted to check for villager with null UUID");
            return false;
        }
        return Main.CONFIG_MANAGER.isVillagerFileExists(villagerId);
    }

    public VillagerMemory concludeMemory(VillagerMemory villager, UUID playerId) throws IOException {
        if (villager == null || playerId == null) {
            throw new IllegalArgumentException("Villager and playerId cannot be null");
        }
        
        var gson = new Gson();
        var model = new Conversation(new ArrayList<>());
        model.addMessage(new Message(ChatRole.SYSTEM, Main.CONFIG_MANAGER.getConclusionPromptTemplate()));
        model.addMessage(new Message(ChatRole.USER, gson.toJson(villager.getConversation(playerId))));
        final var conclusion = Main.CONFIG_MANAGER.getApiConfig().sendConversationAndGetResponseText(model);
        conclusion.ifPresent(m -> {
            var conversation = new Conversation(new ArrayList<>());
            conversation.addMessage(new Message(ChatRole.SYSTEM, m));
            villager.updateConversation(playerId, conversation);
        });

        return villager;
    }

    public String formatVillagerTrades(Villager villager) {
        if (villager == null) {
            return "You don't have any information about trades.";
        }
        
        var trades = villager.getOffers();
        if (trades == null || trades.isEmpty()) {
            return "You don't sell anything for now.";
        }

        var formattedTrades = new ArrayList<String>();
        formattedTrades.add("You ONLY have the following trades:");

        for (var trade : trades) {
            if (trade == null || trade.getCostA() == null || trade.getResult() == null) {
                Main.LOGGER.warn("Encountered null or incomplete trade, skipping");
                continue;
            }
            
            StringBuilder formattedTrade = new StringBuilder();

            formattedTrade.append("- ")
                    .append(trade.getCostA().getCount())
                    .append("x ")
                    .append(trade.getCostA().getHoverName().getString().toLowerCase());

            if (trade.getCostB() != null && !trade.getCostB().isEmpty()) {
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
}
