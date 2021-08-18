package be.garagepoort.mcioc.gui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;

@IocBean
public class TemplateConfigResolver {

    public Object get(String value) {
        return ReflectionUtils.getConfigValue(value, TubingPlugin.getPlugin().getFileConfigurations())
                .orElseThrow(() -> new TubingGuiException("Unknown property defined in permission attribute: [" + value + "]"));
    }
}
