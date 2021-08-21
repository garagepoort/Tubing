package be.garagepoort.mcioc.common;

public interface ITubingBukkitUtil {
    void runAsync(Runnable runnable);

    void runTaskLater(Runnable runnable, int ticks);
}
