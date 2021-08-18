package be.garagepoort.mcioc.permissions;

import be.garagepoort.mcioc.ConditionalOnMissingBean;
import be.garagepoort.mcioc.IocBean;
import org.bukkit.entity.Player;

@IocBean
@ConditionalOnMissingBean
public class DefaultTubingPermissionService implements TubingPermissionService {

    @Override
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

}
