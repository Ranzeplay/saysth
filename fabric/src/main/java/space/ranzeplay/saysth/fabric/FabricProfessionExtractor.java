package space.ranzeplay.saysth.fabric;

import net.fabricmc.loader.api.FabricLoader;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.villager.ProfessionExtractor;

import java.nio.file.Path;

public class FabricProfessionExtractor implements ProfessionExtractor {
    @Override
    public Path getSourcePath() {
        var modContainer = FabricLoader.getInstance().getModContainer(Main.MOD_ID).orElseThrow();
        return modContainer.findPath("assets/professions/").orElseThrow();
    }
}
