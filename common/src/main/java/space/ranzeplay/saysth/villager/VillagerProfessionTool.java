package space.ranzeplay.saysth.villager;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.npc.villager.Villager;
import space.ranzeplay.saysth.chat.Tool;

public final class VillagerProfessionTool implements Tool {
    private final Villager villager;

    public VillagerProfessionTool(Villager villager) {
        if (villager == null) {
            throw new IllegalArgumentException("Villager cannot be null");
        }
        this.villager = villager;
    }

    @Override
    public String getName() {
        return "get_villager_profession";
    }

    @Override
    public String getDescription() {
        return "Get the current profession of the villager.";
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
        return villager.getVillagerData().profession().value().name().getString();
    }
}
