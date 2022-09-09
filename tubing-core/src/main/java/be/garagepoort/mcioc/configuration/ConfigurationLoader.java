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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            throw new ConfigurationException("Could not load TubingConfigurationProvider");
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
            configurationFile.setFileConfiguration(updatedConfig);
        }

        for (ConfigurationFile configurationFile : configurationFiles) {
            String newConfigFile = parseConfigurationPropertiesFromFile(configurationFile.getPath());
            FileConfiguration configuration = ConfigurationUtil.loadConfiguration(newConfigFile);
            configurationFile.setFileConfiguration(configuration);
            this.configurations.add(configuration);
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

    private String parseConfigurationPropertiesFromFile(String configPath) {
        File dataFolder = tubingPlugin.getDataFolder();
        String fullConfigResourcePath = (configPath).replace('\\', '/');
        File configFile = new File(dataFolder, fullConfigResourcePath);

        try {
            FileReader fr = new FileReader(configFile);
            StringBuilder totalStr = new StringBuilder();
            try (BufferedReader br = new BufferedReader(fr)) {

                String line;
                while ((line = br.readLine()) != null) {
                    totalStr.append(line).append(System.getProperty("line.separator"));
                }
                return replaceConfigProperties(totalStr.toString());
            }
        } catch (Exception e) {
            throw new ConfigurationException("Could not replace configuration properties in yaml file");
        }
    }

    private String replaceConfigProperties(String message) {
        String newMessage = message;
        String regexString = Pattern.quote("{{") + "(.*?)" + Pattern.quote("}}");
        Pattern pattern = Pattern.compile(regexString);
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String matched = matcher.group(1);
            Optional<String> configValue = getConfigStringValue(matched);
            if (configValue.isPresent()) {
                newMessage = newMessage.replace("{{" + matched + "}}", configValue.get());
            }
        }
        return newMessage;
    }
}
