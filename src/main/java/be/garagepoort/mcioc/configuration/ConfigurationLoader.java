package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.common.TubingConfigurationProvider;
import be.garagepoort.mcioc.configuration.config.Configuration;
import be.garagepoort.mcioc.configuration.files.AutoUpdater;
import be.garagepoort.mcioc.configuration.files.ConfigMigrator;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;
import be.garagepoort.mcioc.configuration.files.ConfigurationFile;
import be.garagepoort.mcioc.configuration.files.ConfigurationUtil;
import be.garagepoort.mcioc.load.InjectTubingPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@IocBean(priority = true)
public class ConfigurationLoader {

    private List<Configuration> configurations = new ArrayList<>();
    private List<ConfigurationFile> configurationFiles = new ArrayList<>();

    public ConfigurationLoader(@InjectTubingPlugin TubingPlugin tubingPlugin, TubingConfigurationProvider tubingConfigurationProvider) {
        this.configurationFiles = tubingConfigurationProvider.getConfigurationFiles();
        boolean success = loadConfig(tubingPlugin, tubingConfigurationProvider.getConfigurationFiles(), tubingConfigurationProvider.getConfigurationMigrators());
        if (!success) {
            throw new ConfigurationException("Could not load TubingConfigurationProvider");
        }
    }

    private boolean loadConfig(TubingPlugin tubingPlugin, List<ConfigurationFile> configurationFiles, List<ConfigMigrator> configurationMigrators) {
        this.configurations = new ArrayList<>();
        if (configurationFiles.isEmpty()) {
            return true;
        }

        for (ConfigurationFile configurationFile : configurationFiles) {
            ConfigurationUtil.saveConfigFile(tubingPlugin, configurationFile.getPath());
            Configuration currentConfig = ConfigurationUtil.loadConfiguration(tubingPlugin, configurationFile.getPath());
            configurationFile.setFileConfiguration(currentConfig);
        }

        for (ConfigurationFile configurationFile : configurationFiles) {
            AutoUpdater.runMigrations(tubingPlugin, configurationFiles, configurationMigrators);
            Configuration updatedConfig = AutoUpdater.updateConfig(tubingPlugin, configurationFile);
            if (updatedConfig == null) {
                return false;
            }
            this.configurations.add(updatedConfig);
        }
        return true;
    }

    public Map<String, Configuration> getConfigurationFiles() {
        return configurationFiles.stream()
            .collect(Collectors.toMap(ConfigurationFile::getIdentifier, ConfigurationFile::getFileConfiguration, (a, b) -> a));
    }
}
