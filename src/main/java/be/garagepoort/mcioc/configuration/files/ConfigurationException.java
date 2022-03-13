package be.garagepoort.mcioc.configuration.files;

import be.garagepoort.mcioc.TubingPlugin;

public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super("Invalid " + TubingPlugin.getPlugin().getName() + " configuration: [" + message + "]");
    }

    public ConfigurationException(String message, Throwable e) {
        super(message, e);
    }
}
