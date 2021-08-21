package be.garagepoort.mcioc.permissions;

import org.bukkit.entity.Player;

public interface TubingPermissionService {

    boolean has(Player player, String permission);
}
