package be.garagepoort.mcioc.configuration.yaml.configuration;

import be.garagepoort.mcioc.configuration.files.ConfigurationException;

import java.util.Map;

/**
 * This is a {@link Configuration} implementation that does not save or be.garagepoort.mcioc.tubingvelocity.load
 * from any source, and stores all values in memory only.
 * This is useful for temporary Configurations for providing defaults.
 */
public class MemoryConfiguration extends MemorySection implements Configuration {
    protected Configuration defaults;
    protected MemoryConfigurationOptions options;

    /**
     * Creates an empty {@link MemoryConfiguration} with no default values.
     */
    public MemoryConfiguration() {
    }

    /**
     * Creates an empty {@link MemoryConfiguration} using the specified {@link
     * Configuration} as a source for all default values.
     *
     * @param defaults Default value provider
     * @throws IllegalArgumentException Thrown if defaults is null
     */
    public MemoryConfiguration(Configuration defaults) {
        this.defaults = defaults;
    }

    @Override
    public void addDefault(String path, Object value) {
        if(path ==null) {
            throw new ConfigurationException("path must not be null");
        }

        if (defaults == null) {
            defaults = new MemoryConfiguration();
        }

        defaults.set(path, value);
    }

    @Override
    public void addDefaults(Map<String, Object> defaults) {
        if(defaults ==null) {
            throw new ConfigurationException("defaults must not be null");
        }

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            addDefault(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addDefaults(Configuration defaults) {
        if(defaults ==null) {
            throw new ConfigurationException("defaults must not be null");
        }

        for (String key : defaults.getKeys(true)) {
            if (!defaults.isConfigurationSection(key)) {
                addDefault(key, defaults.get(key));
            }
        }
    }

    @Override
    public void setDefaults(Configuration defaults) {
        if(defaults ==null) {
            throw new ConfigurationException("defaults must not be null");
        }

        this.defaults = defaults;
    }

    @Override

    public Configuration getDefaults() {
        return defaults;
    }

    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    @Override

    public MemoryConfigurationOptions options() {
        if (options == null) {
            options = new MemoryConfigurationOptions(this);
        }

        return options;
    }
}
