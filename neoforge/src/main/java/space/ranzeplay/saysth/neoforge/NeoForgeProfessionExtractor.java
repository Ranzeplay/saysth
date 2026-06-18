package space.ranzeplay.saysth.neoforge;

import net.neoforged.fml.ModList;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.villager.ProfessionExtractor;

import java.nio.file.Path;

public class NeoForgeProfessionExtractor implements ProfessionExtractor {
    @Override
    public Path getSourcePath() {
        var modContainer = ModList.get().getModFileById(Main.MOD_ID);
        return modContainer.getFile().getFilePath().resolve("assets", "professions");
    }
}
