package be.garagepoort.mcioc.common;

import be.garagepoort.mcioc.configuration.files.ConfigMigrator;
import be.garagepoort.mcioc.configuration.files.ConfigurationFile;

import java.util.List;

public interface TubingConfigurationProvider {
    List<ConfigMigrator> getConfigurationMigrators();

    List<ConfigurationFile> getConfigurationFiles();
}
