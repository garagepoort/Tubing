package be.garagepoort.mcioc.common;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;

@IocBean
public class TubingPluginProvider {

    public TubingPlugin getPlugin() {
        return TubingPlugin.getPlugin();
    }
}
