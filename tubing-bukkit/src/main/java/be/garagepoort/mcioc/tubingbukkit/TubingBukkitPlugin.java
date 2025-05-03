package be.garagepoort.mcioc.tubingbukkit;

import be.garagepoort.mcioc.IocContainer;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;
import be.garagepoort.mcioc.tubingbukkit.annotations.BeforeTubingReload;
import be.garagepoort.mcioc.tubingbukkit.load.TubingBukkitBeanLoader;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class TubingBukkitPlugin extends JavaPlugin implements TubingPlugin {

    private static TubingBukkitPlugin tubingBukkitPlugin;
    private IocContainer iocContainer = new IocContainer();

    public static TubingBukkitPlugin getPlugin() {
        return tubingBukkitPlugin;
    }

    @Override
    public void onEnable() {
        tubingBukkitPlugin = this;
        beforeEnable();
        
        try {
            iocContainer = initIocContainer();
            TubingBukkitBeanLoader.load(this);
        } catch (ConfigurationException e) {
            this.getLogger().severe(e.getLocalizedMessage());
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        
        enable();
    }

    @Override
    public void onDisable() {
        disable();
    }

    public void reload() {
        List<BeforeTubingReload> beforeTubingReloads = iocContainer.getList(BeforeTubingReload.class);
        if (beforeTubingReloads != null) {
            beforeTubingReloads.forEach(onLoad -> onLoad.execute(this));
        }
        beforeReload();
        reloadConfig();
        HandlerList.unregisterAll(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        try {
            iocContainer = initIocContainer();
            TubingBukkitBeanLoader.load(this);
        } catch (ConfigurationException e) {
            this.getLogger().severe(e.getLocalizedMessage());
            this.getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return super.getClassLoader();
    }

    protected void beforeReload() {
    }

    protected void beforeEnable() {
    }

    protected abstract void enable();

    protected abstract void disable();

    public IocContainer getIocContainer() {
        return iocContainer;
    }

}
