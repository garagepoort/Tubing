package be.garagepoort.mcioc.configuration.files;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigurationFile {

    private final String identifier;
    private final String path;
    private boolean ignoreUpdater = false;
    private FileConfiguration fileConfiguration;

    public ConfigurationFile(String path) {
        this.identifier = getConfigId(path);
        this.path = path;
        ConfigurationUtil.saveConfiguration(path);
        this.fileConfiguration = ConfigurationUtil.loadConfiguration(path);
    }

    public ConfigurationFile(String path, String identifier) {
        this.identifier = identifier;
        this.path = path;
        ConfigurationUtil.saveConfiguration(path);
        this.fileConfiguration = ConfigurationUtil.loadConfiguration(path);
    }

    public ConfigurationFile(String path, FileConfiguration fileConfiguration) {
        this.identifier = getConfigId(path);
        this.path = path;
        this.fileConfiguration = fileConfiguration;
    }

    public ConfigurationFile(String path, String identifier, FileConfiguration fileConfiguration) {
        this.identifier = identifier;
        this.path = path;
        this.fileConfiguration = fileConfiguration;
    }

    public ConfigurationFile(String path, String identifier, boolean ignoreUpdater) {
        this.identifier = identifier;
        this.path = path;
        ConfigurationUtil.saveConfiguration(path);
        this.fileConfiguration = ConfigurationUtil.loadConfiguration(path);
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

    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    private String getConfigId(String path) {
        return path.replace("/", "-")
            .replace(".yml", "");
    }
}
