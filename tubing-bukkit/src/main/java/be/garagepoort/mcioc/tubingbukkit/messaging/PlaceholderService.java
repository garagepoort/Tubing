package be.garagepoort.mcioc.tubingbukkit.messaging;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.load.InjectTubingPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@IocBean
public class PlaceholderService {

    private boolean usesPlaceholderAPI = false;

    public PlaceholderService(@InjectTubingPlugin TubingPlugin tubingPlugin) {
        Plugin placeholderPlugin;
        if ((placeholderPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")) != null) {
            usesPlaceholderAPI = true;
            tubingPlugin.getLogger().info("Hooked into PlaceholderAPI " + placeholderPlugin.getDescription().getVersion());
        }
    }

    public String setPlaceholders(CommandSender sender, String message) {
        if (!usesPlaceholderAPI || !(sender instanceof Player)) {
            return message;
        }
        return PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, message);
    }

    public String setPlaceholders(Player sender, String message) {
        if (!usesPlaceholderAPI || sender == null) {
            return message;
        }
        return PlaceholderAPI.setPlaceholders(sender, message);
    }
}
