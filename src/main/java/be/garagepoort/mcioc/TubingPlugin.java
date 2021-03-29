package be.garagepoort.mcioc;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class TubingPlugin extends JavaPlugin {

    private static TubingPlugin plugin;
    private final IocContainer iocContainer = new IocContainer();

    public static TubingPlugin get() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        enable();
        iocContainer.init(this, getConfig());
    }

    @Override
    public void onDisable() {
        disable();
    }

    protected abstract void enable();

    protected abstract void disable();

    public IocContainer getIocContainer() {
        return iocContainer;
    }

}
