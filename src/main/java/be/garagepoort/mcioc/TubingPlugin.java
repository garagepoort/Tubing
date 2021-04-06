package be.garagepoort.mcioc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class TubingPlugin extends JavaPlugin {

    private IocContainer iocContainer = new IocContainer();

    @Override
    public void onEnable() {
        enable();
        iocContainer.init(this, getFileConfigurations());
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
        iocContainer.init(this, getFileConfigurations());
    }

    protected void beforeReload() {}

    protected abstract void enable();

    protected abstract void disable();

    public Map<String, FileConfiguration> getFileConfigurations() {
        HashMap<String, FileConfiguration> configs = new HashMap<>();
        configs.put("config", getConfig());
        return configs;
    }

    public IocContainer getIocContainer() {
        return iocContainer;
    }

}
