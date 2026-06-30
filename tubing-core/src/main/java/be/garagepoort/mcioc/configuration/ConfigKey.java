package be.garagepoort.mcioc.configuration;

import java.util.Map;
import java.util.Optional;

import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;
import be.garagepoort.mcioc.diagnostics.TubingDiagnosticException;

import java.util.Arrays;
import java.util.Collections;

public class ConfigKey {

    private final String original;
    private final String fileId;
    private final String path;

    private ConfigKey(String original, String fileId, String path) {
        this.original = original;
        this.fileId = fileId;
        this.path = path;
    }

    public static ConfigKey parse(String identifier) {
        String configFileId = "config";
        String path = identifier;

        String[] fileSelectors = identifier.split(":", 2);
        if (fileSelectors.length == 2) {
            configFileId = fileSelectors[0];
            path = fileSelectors[1];
        }
        return new ConfigKey(identifier, configFileId, path);
    }

    public Optional<Object> getValue(Map<String, FileConfiguration> configs) {
        FileConfiguration config = getConfiguration(configs);
        if (config == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(config.get(path));
    }

    public Optional<String> getStringValue(Map<String, FileConfiguration> configs) {
        FileConfiguration config = getConfiguration(configs);
        if (config == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(config.getString(path));
    }

    public FileConfiguration requireConfiguration(Map<String, FileConfiguration> configs) {
        FileConfiguration config = getConfiguration(configs);
        if (config == null) {
            throw new TubingDiagnosticException(
                "Unknown configuration file",
                Arrays.asList(
                    "Property: " + original,
                    "Unknown file id: " + fileId,
                    "Known file ids: " + (configs.isEmpty() ? "(none)" : configs.keySet())
                ),
                Collections.singletonList("Use an existing configuration file id, or register a ConfigurationFile with id '" + fileId + "'.")
            );
        }
        return config;
    }

    private FileConfiguration getConfiguration(Map<String, FileConfiguration> configs) {
        return configs.get(fileId);
    }

    public String getOriginal() {
        return original;
    }

    public String getFileId() {
        return fileId;
    }

    public String getPath() {
        return path;
    }
}
