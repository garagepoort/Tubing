package be.garagepoort.mcioc;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class TubingPlugin extends JavaPlugin {

    private IocContainer iocContainer = new IocContainer();

    @Override
    public void onEnable() {
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
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        iocContainer = new IocContainer();
        iocContainer.init(this, getConfig());
    }

    protected void beforeReload() {}

    protected abstract void enable();

    protected abstract void disable();

    public IocContainer getIocContainer() {
        return iocContainer;
    }

}
