package be.garagepoort.mcioc.tubingbukkit.common;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.tubingbukkit.TubingBukkitPlugin;
import org.bukkit.Bukkit;

@IocBean
public class TubingBukkitUtil implements ITubingBukkitUtil {

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(TubingBukkitPlugin.getPlugin(), runnable);
    }

    @Override
    public void runTaskLater(Runnable runnable, int ticks) {
        Bukkit.getScheduler().runTaskLater(TubingBukkitPlugin.getPlugin(), runnable, ticks);
    }
}
