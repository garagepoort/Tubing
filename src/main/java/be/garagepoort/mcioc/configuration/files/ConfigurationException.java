package be.garagepoort.mcioc.configuration.files;

import be.garagepoort.mcioc.TubingPlugin;

public class ConfigurationException extends RuntimeException {

    public ConfigurationException(TubingPlugin tubingPlugin, String message) {
        super("Invalid " + tubingPlugin.getName() + " configuration: [" + message + "]");
    }

    public ConfigurationException(String message) {
        super("Invalid configuration: [" + message + "]");
    }

    public ConfigurationException(String message, Throwable e) {
        super(message, e);
    }
}
