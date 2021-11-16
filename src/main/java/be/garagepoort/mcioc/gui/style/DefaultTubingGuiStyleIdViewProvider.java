package be.garagepoort.mcioc.gui.style;

import be.garagepoort.mcioc.ConditionalOnMissingBean;
import be.garagepoort.mcioc.IocBean;
import org.bukkit.entity.Player;

@IocBean
@ConditionalOnMissingBean
public class DefaultTubingGuiStyleIdViewProvider implements TubingGuiStyleIdViewProvider {
    @Override
    public boolean canView(Player player) {
        return false;
    }
}
