package space.ranzeplay.saysth.instant;

import lombok.AllArgsConstructor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;

@AllArgsConstructor
public class InstantDataProvider {
    Villager villager;
    Player player;

    public long getWorldTime() {
        return villager.getCommandSenderWorld().getDayTime() % 24000; // Get the current time of the world
    }

    public String getWorldTimeFormatted() {
        long time = getWorldTime();
        long hour = time / 1000 + 6; // 6:00 is the start of the day
        long minute = (time % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hour, minute);
    }

    public int getHappiness() {
        return -villager.getUnhappyCounter();
    }

    public int getReputation() {
        return villager.getPlayerReputation(player);
    }

    public int getXp() {
        return villager.getVillagerXp();
    }

    public int getAge(){
        return villager.getAge();
    }

    public MerchantOffers getOffers(){
        return villager.getOffers();
    }
}
