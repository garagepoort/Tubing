package be.garagepoort.mcioc.common;

import be.garagepoort.mcioc.IocBean;
import org.bukkit.Bukkit;

@IocBean
public class TubingBukkitUtil implements ITubingBukkitUtil {

    private final TubingPluginProvider tubingPluginProvider;

    public TubingBukkitUtil(TubingPluginProvider tubingPluginProvider) {
        this.tubingPluginProvider = tubingPluginProvider;
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(tubingPluginProvider.getPlugin(), runnable);
    }

    @Override
    public void runTaskLater(Runnable runnable, int ticks) {
        Bukkit.getScheduler().runTaskLater(tubingPluginProvider.getPlugin(), runnable, ticks);
    }
}
