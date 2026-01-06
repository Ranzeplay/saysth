package space.ranzeplay.saysth.instant;

import lombok.AllArgsConstructor;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;

@AllArgsConstructor
public class InstantDataProvider {
    Villager villager;
    Player player;

    public long getWorldTime() {
        if (villager == null || villager.level() == null) {
            return 0;
        }
        return villager.level().getDayTime() % 24000; // Get the current time of the world
    }

    public String getWorldTimeFormatted() {
        long time = getWorldTime();
        long hour = (time / 1000 + 6) % 24; // 6:00 is the start of the day, wrap at 24
        long minute = (time % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hour, minute);
    }

    public int getHappiness() {
        if (villager == null) {
            return 0;
        }
        return -villager.getUnhappyCounter();
    }

    public int getReputation() {
        if (villager == null || player == null) {
            return 0;
        }
        return villager.getPlayerReputation(player);
    }

    public int getXp() {
        if (villager == null) {
            return 0;
        }
        return villager.getVillagerXp();
    }

    public int getAge(){
        if (villager == null) {
            return 0;
        }
        return villager.getAge();
    }

    public MerchantOffers getOffers(){
        if (villager == null) {
            return new MerchantOffers();
        }
        return villager.getOffers();
    }
}
