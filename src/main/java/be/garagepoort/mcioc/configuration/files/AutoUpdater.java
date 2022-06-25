package be.garagepoort.mcioc.configuration.files;

import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.configuration.yaml.configuration.ConfigurationSection;
import be.garagepoort.mcioc.configuration.yaml.configuration.InvalidConfigurationException;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoUpdater {

    public static FileConfiguration updateConfig(TubingPlugin tubingPlugin, ConfigurationFile configurationFile) {
        if (configurationFile.isIgnoreUpdater()) {
            return configurationFile.getFileConfiguration();
        }

        try {
            validateConfigFile(tubingPlugin, configurationFile.getPath());

            FileConfiguration config = configurationFile.getFileConfiguration();
            FileConfiguration newConfig = new YamlConfiguration();

            AtomicInteger counter = new AtomicInteger();
            Map<String, Object> defaultConfigMap = loadConfig(configurationFile.getPath());

            defaultConfigMap.forEach((k, v) -> {
                if (!config.contains(k) && !(v instanceof ConfigurationSection)) {
                    newConfig.set(k, v);
                    counter.getAndIncrement();
                } else {
                    newConfig.set(k, config.get(k));
                }
            });

            config.getKeys(true).forEach((k) -> {
                Object value = config.get(k);
                if (!newConfig.contains(k) && !(value instanceof ConfigurationSection)) {
                    config.set(k, value);
                }
            });

            File file = new File(tubingPlugin.getDataFolder() + File.separator + configurationFile.getPath());
            newConfig.save(file);
            configurationFile.setFileConfiguration(newConfig);
            return newConfig;
        } catch (InvalidConfigurationException | IOException | ConfigurationException e) {
           tubingPlugin.getLogger().severe("Configuration file is INVALID!!! Disabling " +tubingPlugin.getLogger() + "!");
           tubingPlugin.getLogger().severe("Full error [" + e.getMessage() + "]");
            return null;
        }
    }

    public static void runMigrations(TubingPlugin tubingPlugin, List<ConfigurationFile> fileConfigurations, List<ConfigMigrator> configMigrators) {
        try {
            configMigrators.forEach(m -> m.migrate(fileConfigurations));
            for (ConfigurationFile configurationFile : fileConfigurations) {
                File file = new File(tubingPlugin.getDataFolder() + File.separator + configurationFile.getPath());
                configurationFile.getFileConfiguration().options().copyDefaults(true);
                configurationFile.getFileConfiguration().save(file);
            }
        } catch (IOException e) {
            throw new ConfigurationException("Unable to migrate configurations", e);
        }
    }

    private static Map<String, Object> loadConfig(String filename) {
        Map<String, Object> configurations = new LinkedHashMap<>();
        InputStream defConfigStream = getResource(filename);
        if (defConfigStream != null) {
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            Set<String> keys = yamlConfiguration.getKeys(true);
            keys.forEach(k -> configurations.put(k, yamlConfiguration.get(k)));
        }
        return configurations;
    }

    private static void validateConfigFile(TubingPlugin tubingPlugin, String filename) throws IOException, InvalidConfigurationException {
        validateConfigFile(tubingPlugin.getDataFolder(), filename);
    }

    private static void validateConfigFile(File folder, String filename) throws IOException, InvalidConfigurationException {
        File file = new File(folder, filename);
        if (!file.exists()) {
            throw new ConfigurationException("No configuration file found");
        }
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);
    }

    private static InputStream getResource(String filename) {
        try {
            URL url = AutoUpdater.class.getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException var4) {
            return null;
        }
    }
}
