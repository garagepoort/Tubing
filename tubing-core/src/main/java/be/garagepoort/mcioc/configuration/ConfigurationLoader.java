package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.common.TubingConfigurationProvider;
import be.garagepoort.mcioc.configuration.files.AutoUpdater;
import be.garagepoort.mcioc.configuration.files.ConfigMigrator;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;
import be.garagepoort.mcioc.configuration.files.ConfigurationFile;
import be.garagepoort.mcioc.configuration.files.ConfigurationUtil;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;
import be.garagepoort.mcioc.load.InjectTubingPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@IocBean(priority = true)
public class ConfigurationLoader {

    private List<FileConfiguration> configurations = new ArrayList<>();
    private final TubingPlugin tubingPlugin;
    private List<ConfigurationFile> configurationFiles = new ArrayList<>();

    public ConfigurationLoader(@InjectTubingPlugin TubingPlugin tubingPlugin, TubingConfigurationProvider tubingConfigurationProvider) {
        this.tubingPlugin = tubingPlugin;
        this.configurationFiles = tubingConfigurationProvider.getConfigurationFiles();
        boolean success = loadConfig(tubingPlugin, tubingConfigurationProvider.getConfigurationMigrators());
        if (!success) {
            throw new ConfigurationException("Could not be.garagepoort.mcioc.tubingvelocity.load TubingConfigurationProvider");
        }
    }

    private boolean loadConfig(TubingPlugin tubingPlugin, List<ConfigMigrator> configurationMigrators) {
        this.configurations = new ArrayList<>();
        if (configurationFiles.isEmpty()) {
            return true;
        }

        for (ConfigurationFile configurationFile : configurationFiles) {
            ConfigurationUtil.saveConfigFile(tubingPlugin, configurationFile.getPath());
            FileConfiguration currentConfig = ConfigurationUtil.loadConfiguration(tubingPlugin, configurationFile.getPath());
            configurationFile.setFileConfiguration(currentConfig);
        }

        for (ConfigurationFile configurationFile : configurationFiles) {
            AutoUpdater.runMigrations(tubingPlugin, configurationFiles, configurationMigrators);
            FileConfiguration updatedConfig = AutoUpdater.updateConfig(tubingPlugin, configurationFile);
            if (updatedConfig == null) {
                return false;
            }
            this.configurations.add(updatedConfig);
        }
        return true;
    }

    public Map<String, FileConfiguration> getConfigurationFiles() {
        return configurationFiles.stream()
            .collect(Collectors.toMap(ConfigurationFile::getIdentifier, ConfigurationFile::getFileConfiguration, (a, b) -> a));
    }

    public <T> Optional<T> getConfigValue(String identifier) {
        return ReflectionUtils.getConfigValue(identifier, getConfigurationFiles());
    }

    public Optional<String> getConfigStringValue(String identifier) {
        return ReflectionUtils.getConfigStringValue(identifier, getConfigurationFiles());
    }
}
