package space.ranzeplay.saysth.events;

import com.github.pemistahl.lingua.api.Language;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.time.StopWatch;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.chat.Translator;
import space.ranzeplay.saysth.villager.VillagerMemory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerChatEvent {
    public static void onPlayerChat(ServerPlayer player, String message) throws IOException {
        if (!message.startsWith("$")) return;

        final var nearbyVillagers = getNearbyVillagers(player);

        for (final var villager : nearbyVillagers) {
            final var memory = Main.VILLAGER_MANAGER.getVillager(villager);
            villager.setCustomName(Component.literal(memory.getName()));

            performAIChat(player, message, memory);
        }
    }

    private static void performAIChat(ServerPlayer player, String message, VillagerMemory memory) {
        new Thread(() -> {
            var stopwatch = StopWatch.createStarted();
            var threadMessage = message;
            final var lang = Main.LANGUAGE_DETECTOR.detectLanguageOf(threadMessage);
            player.sendSystemMessage(Component.literal(String.format("Detected language: %s", lang.name())));
            if(lang != Language.ENGLISH) {
                try {
                    threadMessage = Translator.translate(lang, Language.ENGLISH, threadMessage);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            String response;
            try {
                response = Main.VILLAGER_MANAGER.sendMessageToVillager(memory.getId(), player.getUUID(), threadMessage);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (!response.equals("IGN")) {
                if(lang != Language.ENGLISH) {
                    try {
                        response = Translator.translate(Language.ENGLISH, lang, response);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                player.sendSystemMessage(Component
                        .literal(String.format("<(Villager) %s> %s", memory.getName(), response))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.AQUA)))
                );
                player.sendSystemMessage(Component.literal(String.format("Time consumed: %dms",stopwatch.getTime())));
            }

            stopwatch.stop();
        }).start();
    }

    private static List<Villager> getNearbyVillagers(ServerPlayer player) {
        final var look = player.getLookAngle();
        final var position = player.getEyePosition().add(look.scale(4));
        final var posA = position.add(-5, -5, -5);
        final var posB = position.add(5, 5, 5);
        return player.getCommandSenderWorld().getEntitiesOfClass(Villager.class, new AABB(posA, posB));
    }
}
