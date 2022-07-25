package be.garagepoort.mcioc.tubinggui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.configuration.ConfigurationLoader;
import be.garagepoort.mcioc.tubinggui.exceptions.TubingGuiException;

@IocBean
public class TemplateConfigResolver {

    private final ConfigurationLoader configurationLoader;

    public TemplateConfigResolver(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    public Object get(String value) {
        return ReflectionUtils.getConfigValue(value, configurationLoader.getConfigurationFiles())
                .orElseThrow(() -> new TubingGuiException("Unknown property defined in permission attribute: [" + value + "]"));
    }
}
