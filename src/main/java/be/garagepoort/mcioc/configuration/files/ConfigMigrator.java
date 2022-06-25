package be.garagepoort.mcioc.configuration.files;

import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;

import java.util.List;

public interface ConfigMigrator {

    void migrate(List<ConfigurationFile> config);

    default FileConfiguration getConfig(List<ConfigurationFile> configs, String identifier) {
        return configs.stream().filter(c -> c.getIdentifier().equalsIgnoreCase(identifier)).findFirst().map(ConfigurationFile::getFileConfiguration).orElse(null);
    }
    
}
