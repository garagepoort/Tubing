package be.garagepoort.mcioc.configuration.files;

import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.configuration.config.Configuration;
import be.garagepoort.mcioc.configuration.config.ConfigurationProvider;
import be.garagepoort.mcioc.configuration.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AutoUpdater {

    public static Configuration updateConfig(TubingPlugin tubingPlugin, ConfigurationFile configurationFile) {
        if (configurationFile.isIgnoreUpdater()) {
            return configurationFile.getFileConfiguration();
        }

        try {
            validateConfigFile(tubingPlugin, configurationFile.getPath());

            Configuration defaultConfig = loadConfig(configurationFile.getPath());
            Configuration newConfig = new Configuration();

            fillNewConfig(null, defaultConfig, configurationFile.getFileConfiguration(), newConfig);

            File file = new File(tubingPlugin.getDataFolder() + File.separator + configurationFile.getPath());
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(newConfig, file);
            return newConfig;
        } catch (IOException | ConfigurationException e) {
            tubingPlugin.getLogger().severe("Configuration file is INVALID!!! Disabling " + tubingPlugin.getLogger() + "!");
            tubingPlugin.getLogger().severe("Full error [" + e.getMessage() + "]");
            return null;
        }
    }

    private static void fillNewConfig(String parentPath, Configuration defaultConfigSection, Configuration currentConfig, Configuration newConfig) {
        for (String key : defaultConfigSection.getKeys(false)) {
            Object value = defaultConfigSection.get(key);
            String fullPath = parentPath == null ? key : parentPath + "." + key;
            if (value instanceof Configuration) {
                fillNewConfig(fullPath, defaultConfigSection.getSection(key), currentConfig, newConfig);
            } else {
                Object defaultValue = defaultConfigSection.get(key);
                Object currentValue = currentConfig.get(fullPath);
                if (currentValue == null) {
                    newConfig.set(fullPath, defaultValue);
                } else {
                    newConfig.set(fullPath, currentValue);
                }
            }
        }
    }

    public static void runMigrations(TubingPlugin tubingPlugin, List<ConfigurationFile> fileConfigurations, List<ConfigMigrator> configMigrators) {
        try {
            configMigrators.forEach(m -> m.migrate(fileConfigurations));
            for (ConfigurationFile configurationFile : fileConfigurations) {
                File file = new File(tubingPlugin.getDataFolder() + File.separator + configurationFile.getPath());
                ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
                provider.save(configurationFile.getFileConfiguration(), file);
            }
        } catch (IOException e) {
            throw new ConfigurationException("Unable to migrate configurations", e);
        }
    }

    private static Configuration loadConfig(String filename) {
        InputStream defConfigStream = getResource(filename);
        if (defConfigStream != null) {
            ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
            return provider.load(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
        }
        return new Configuration();
    }

    private static void validateConfigFile(TubingPlugin tubingPlugin, String filename) throws IOException {
        validateConfigFile(tubingPlugin, tubingPlugin.getDataFolder(), filename);
    }

    private static void validateConfigFile(TubingPlugin tubingPlugin, File folder, String filename) throws IOException {
        File file = new File(folder, filename);
        if (!file.exists()) {
            throw new ConfigurationException(tubingPlugin, "No configuration file found");
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
