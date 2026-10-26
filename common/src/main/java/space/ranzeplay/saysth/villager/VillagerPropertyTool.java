package space.ranzeplay.saysth.villager;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import space.ranzeplay.saysth.chat.Tool;
import space.ranzeplay.saysth.instant.InstantDataProvider;

public final class VillagerPropertyTool implements Tool {
    public enum Property {
        WORLD_TIME("get_villager_world_time", "Get the current world time for the villager."),
        HAPPINESS("get_villager_happiness", "Get the villager's current happiness value."),
        REPUTATION("get_villager_reputation", "Get the villager's reputation with the current player."),
        EXPERIENCE("get_villager_experience", "Get the villager's current trading experience."),
        AGE("get_villager_age", "Get the villager's current age."),
        TRADES("get_villager_trades", "Get the villager's current available trades.");

        private final String name;
        private final String description;

        Property(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private final Property property;
    private final InstantDataProvider dataProvider;

    public VillagerPropertyTool(Property property, Villager villager, Player player) {
        if (property == null) {
            throw new IllegalArgumentException("Property cannot be null");
        }
        this.property = property;
        this.dataProvider = new InstantDataProvider(villager, player);
    }

    @Override
    public String getName() {
        return property.name;
    }

    @Override
    public String getDescription() {
        return property.description;
    }

    @Override
    public JsonObject getParameters() {
        var parameters = new JsonObject();
        parameters.addProperty("type", "object");
        parameters.add("properties", new JsonObject());
        parameters.add("required", new com.google.gson.JsonArray());
        return parameters;
    }

    @Override
    public String execute(JsonObject arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("Tool arguments cannot be null");
        }
        return switch (property) {
            case WORLD_TIME -> dataProvider.getWorldTimeFormatted()
                    + " (world ticks: " + dataProvider.getWorldTime() + ")";
            case HAPPINESS -> Integer.toString(dataProvider.getHappiness());
            case REPUTATION -> Integer.toString(dataProvider.getReputation());
            case EXPERIENCE -> Integer.toString(dataProvider.getXp());
            case AGE -> Integer.toString(dataProvider.getAge());
            case TRADES -> VillagerTradeFormatter.format(dataProvider.getOffers());
        };
    }
}
