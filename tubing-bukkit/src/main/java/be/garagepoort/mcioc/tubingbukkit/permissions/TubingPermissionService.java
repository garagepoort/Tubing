package be.garagepoort.mcioc.tubingbukkit.permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface TubingPermissionService {

    boolean has(Player player, String permission);

    boolean has(CommandSender player, String permission);
}
