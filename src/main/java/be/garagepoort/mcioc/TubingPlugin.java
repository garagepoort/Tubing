package be.garagepoort.mcioc;

import be.garagepoort.mcioc.gui.GuiActionService;
import be.garagepoort.mcioc.gui.InventoryClick;
import be.garagepoort.mcioc.gui.InventoryClose;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class TubingPlugin extends JavaPlugin {

    private static TubingPlugin tubingPlugin;
    private IocContainer iocContainer = new IocContainer();

    public static TubingPlugin getPlugin() {
        return tubingPlugin;
    }

    @Override
    public void onEnable() {
        enable();
        tubingPlugin = this;
        iocContainer.init(this, getFileConfigurations());
        iocContainer.get(GuiActionService.class).loadGuiControllers();

        Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClose(), this);
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
        iocContainer.get(GuiActionService.class).loadGuiControllers();

        Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClose(), this);
    }

    protected void beforeReload() {
    }

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
