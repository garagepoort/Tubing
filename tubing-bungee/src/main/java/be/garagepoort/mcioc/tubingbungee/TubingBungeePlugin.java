package be.garagepoort.mcioc.tubingbungee;

import be.garagepoort.mcioc.IocContainer;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.tubingbungee.annotations.BeforeTubingReload;
import be.garagepoort.mcioc.tubingbungee.load.TubingBungeeBeanLoader;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;

public abstract class TubingBungeePlugin extends Plugin implements TubingPlugin {

    private static TubingBungeePlugin tubingBungeePlugin;
    private IocContainer iocContainer = new IocContainer();

    public static TubingBungeePlugin getPlugin() {
        return tubingBungeePlugin;
    }

    @Override
    public void onEnable() {
        tubingBungeePlugin = this;
        beforeEnable();
        iocContainer = initIocContainer();
        TubingBungeeBeanLoader.load(this);
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
        ProxyServer.getInstance().getPluginManager().unregisterCommands(this);
        ProxyServer.getInstance().getPluginManager().unregisterListeners(this);

        iocContainer = initIocContainer();
        TubingBungeeBeanLoader.load(this);
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return super.getClass().getClassLoader();
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
