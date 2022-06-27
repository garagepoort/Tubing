package be.garagepoort.mcioc.tubingvelocity;

import be.garagepoort.mcioc.IocContainer;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.tubingvelocity.annotations.BeforeTubingReload;
import be.garagepoort.mcioc.tubingvelocity.load.TubingVelocityBeanLoader;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public abstract class TubingVelocityPlugin implements TubingPlugin {

    private static TubingVelocityPlugin tubingBungeePlugin;

    private IocContainer iocContainer;
    private final File dataFolder;
    private final ProxyServer proxyServer;

    public static TubingVelocityPlugin getPlugin() {
        return tubingBungeePlugin;
    }

    public TubingVelocityPlugin(ProxyServer server, @DataDirectory final Path folder) {
        this.proxyServer = server;
        this.dataFolder = folder.toFile();

    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        tubingBungeePlugin = this;
        beforeEnable();
        iocContainer = initIocContainer();
        TubingVelocityBeanLoader.load(this);
        enable();
    }
    protected abstract void enable();

    @Override
    public java.util.logging.Logger getLogger() {
        return java.util.logging.Logger.getLogger(getName());
    }

    public void reload() {
        List<BeforeTubingReload> beforeTubingReloads = iocContainer.getList(BeforeTubingReload.class);
        if (beforeTubingReloads != null) {
            beforeTubingReloads.forEach(onLoad -> onLoad.execute(this));
        }
        beforeReload();
        for (String alias : this.proxyServer.getCommandManager().getAliases()) {
            this.proxyServer.getCommandManager().unregister(alias);
        }
        this.proxyServer.getEventManager().unregisterListeners(this);

        iocContainer = initIocContainer();
        TubingVelocityBeanLoader.load(this);
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return super.getClass().getClassLoader();
    }

    protected void beforeReload() {
    }

    protected void beforeEnable() {
    }

    public IocContainer getIocContainer() {
        return iocContainer;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    public CommandManager getCommandManager() {
        return proxyServer.getCommandManager();
    }

    public EventManager getEventManager() {
        return proxyServer.getEventManager();
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }
}
