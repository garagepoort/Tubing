package be.garagepoort.mcioc;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.Objects;

public class IocConditionalPropertyFilter {

    public boolean isValidBean(Class a, Map<String, FileConfiguration> configs) {
        IocBean annotation = (IocBean) a.getAnnotation(IocBean.class);
        String conditionalOnProperty = annotation.conditionalOnProperty();
        String configFileId = "config";
        if (!StringUtils.isEmpty(conditionalOnProperty)) {
            String[] fileSelectors = conditionalOnProperty.split(":", 2);
            if(fileSelectors.length == 2) {
                configFileId = fileSelectors[0];
                conditionalOnProperty = fileSelectors[1];
            }
            String[] split = conditionalOnProperty.split("=",2);
            String key = split[0];
            String value = split[1];
            return Objects.requireNonNull(configs.get(configFileId).getString(key)).equalsIgnoreCase(value);
        }
        return true;
    }
}
