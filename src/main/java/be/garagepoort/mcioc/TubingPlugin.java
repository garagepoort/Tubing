package be.garagepoort.mcioc;

import be.garagepoort.mcioc.configuration.files.AutoUpdater;
import be.garagepoort.mcioc.configuration.files.ConfigMigrator;
import be.garagepoort.mcioc.configuration.files.ConfigurationFile;
import be.garagepoort.mcioc.gui.GuiActionService;
import be.garagepoort.mcioc.gui.InventoryClick;
import be.garagepoort.mcioc.gui.InventoryClose;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TubingPlugin extends JavaPlugin {

    private static TubingPlugin tubingPlugin;
    private IocContainer iocContainer = new IocContainer();
    private List<ConfigurationFile> configurationFiles;

    public static TubingPlugin getPlugin() {
        return tubingPlugin;
    }

    @Override
    public void onEnable() {
        tubingPlugin = this;
        beforeEnable();
        if (!loadConfig()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        iocContainer.init(this, getFileConfigurations());
        iocContainer.get(GuiActionService.class).loadGuiControllers();

        Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClose(), this);

        enable();
    }

    @Override
    public void onDisable() {
        disable();
    }

    public void reload() {
        beforeReload();
        loadConfig();
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

    protected void beforeEnable() {
    }

    protected abstract void enable();

    protected abstract void disable();

    public Map<String, FileConfiguration> getFileConfigurations() {
        return configurationFiles.stream()
            .collect(Collectors.toMap(ConfigurationFile::getIdentifier, ConfigurationFile::getFileConfiguration, (a, b) -> a));
    }

    public List<ConfigMigrator> getConfigurationMigrators() {
        return Collections.emptyList();
    }

    public IocContainer getIocContainer() {
        return iocContainer;
    }

    public List<ConfigurationFile> getConfigurationFiles() {
        InputStream defConfigStream = this.getResource("config.yml");
        if (defConfigStream != null) {
            return Collections.singletonList(new ConfigurationFile("config.yml"));
        } else {
            return Collections.emptyList();
        }
    }

    private boolean loadConfig() {
        configurationFiles = getConfigurationFiles();
        if (configurationFiles.isEmpty()) {
            return true;
        }
        saveDefaultConfig();
        AutoUpdater.runMigrations(configurationFiles, getConfigurationMigrators());
        for (ConfigurationFile c : configurationFiles) {
            if (!AutoUpdater.updateConfig(c)) {
                Bukkit.getPluginManager().disablePlugin(this);
                return false;
            }
        }
        return true;
    }
}
