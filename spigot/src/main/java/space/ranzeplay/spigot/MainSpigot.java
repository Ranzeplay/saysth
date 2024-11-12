package space.ranzeplay.spigot;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.events.PlayerChatEvent;

import java.io.IOException;

public final class MainSpigot extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Main.init(getDataFolder().toPath(), Utils.javaLoggerToSlf4j(getLogger()));

        getServer().getPluginManager().registerEvents(new MainSpigot(), this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) throws IOException {
        PlayerChatEvent.onPlayerChat((ServerPlayer) event.getPlayer(), event.getMessage());
    }
}
