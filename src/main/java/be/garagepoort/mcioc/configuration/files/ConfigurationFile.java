package be.garagepoort.mcioc.configuration.files;

import be.garagepoort.mcioc.configuration.config.Configuration;

public class ConfigurationFile {

    private final String identifier;
    private final String path;
    private boolean ignoreUpdater = false;
    private Configuration fileConfiguration;

    public ConfigurationFile(String path) {
        this.identifier = getConfigId(path);
        this.path = path;
    }

    public ConfigurationFile(String path, String identifier) {
        this.identifier = identifier;
        this.path = path;
    }

    public ConfigurationFile(String path, String identifier, boolean ignoreUpdater) {
        this.identifier = identifier;
        this.path = path;
        this.ignoreUpdater = ignoreUpdater;
    }

    public boolean isIgnoreUpdater() {
        return ignoreUpdater;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPath() {
        return path;
    }

    public void setFileConfiguration(Configuration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public Configuration getFileConfiguration() {
        return fileConfiguration;
    }

    private String getConfigId(String path) {
        return path.replace("/", "-")
            .replace(".yml", "");
    }
}
