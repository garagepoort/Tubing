package be.garagepoort.mcioc;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class TubingPlugin extends JavaPlugin {

    private static TubingPlugin plugin;
    private IocContainer iocContainer = new IocContainer();

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

    public void reload() {
        beforeReload();
        reloadConfig();
        HandlerList.unregisterAll(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);

        iocContainer = new IocContainer();
        iocContainer.init(plugin, getConfig());
    }

    protected void beforeReload() {}

    protected abstract void enable();

    protected abstract void disable();

    public IocContainer getIocContainer() {
        return iocContainer;
    }

}
