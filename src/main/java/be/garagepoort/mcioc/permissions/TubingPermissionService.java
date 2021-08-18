package be.garagepoort.mcioc.permissions;

import org.bukkit.entity.Player;

public interface TubingPermissionService {

    boolean hasPermission(Player player, String permission);
}
