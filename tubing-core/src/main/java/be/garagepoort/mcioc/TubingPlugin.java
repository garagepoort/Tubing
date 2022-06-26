package be.garagepoort.mcioc;

import be.garagepoort.mcioc.load.OnLoad;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public interface TubingPlugin {

    String getName();

    ClassLoader getPluginClassLoader();

    File getDataFolder();

    Logger getLogger();

    IocContainer getIocContainer();

    default IocContainer initIocContainer() {
        IocContainer iocContainer = new IocContainer();
        iocContainer.init(this);
        List<OnLoad> onloads = iocContainer.getList(OnLoad.class);
        if (onloads != null) {
            onloads.forEach(onLoad -> onLoad.load(iocContainer));
        }
        return iocContainer;
    }

}
