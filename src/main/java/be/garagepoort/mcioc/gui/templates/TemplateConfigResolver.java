package be.garagepoort.mcioc.gui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.common.TubingConfigurationProvider;
import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;

@IocBean
public class TemplateConfigResolver {

    private final TubingConfigurationProvider tubingConfigurationProvider;

    public TemplateConfigResolver(TubingConfigurationProvider tubingConfigurationProvider) {
        this.tubingConfigurationProvider = tubingConfigurationProvider;
    }

    public Object get(String value) {
        return ReflectionUtils.getConfigValue(value, tubingConfigurationProvider.getConfigurations())
                .orElseThrow(() -> new TubingGuiException("Unknown property defined in permission attribute: [" + value + "]"));
    }
}
