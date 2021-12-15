package be.garagepoort.mcioc.common;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

@IocBean
public class TubingConfigurationProvider {

    public Map<String, FileConfiguration> getConfigurations() {
        return TubingPlugin.getPlugin().getFileConfigurations();
    }
}
