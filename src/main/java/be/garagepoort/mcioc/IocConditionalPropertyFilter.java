package be.garagepoort.mcioc;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class IocConditionalPropertyFilter {

    public boolean isValidBean(Class a, Map<String, FileConfiguration> configs) {
        IocBean annotation = (IocBean) a.getAnnotation(IocBean.class);
        String conditionalOnProperty = annotation.conditionalOnProperty();
        if (!StringUtils.isEmpty(conditionalOnProperty)) {
            String[] split = conditionalOnProperty.split("=", 2);
            String key = split[0];
            String value = split[1];

            String configValue = ReflectionUtils.getConfigStringValue(key, configs)
                    .orElseThrow(() -> new IocException("ConditionOnProperty referencing an unknown property [" + key + "]"));

            return configValue.equalsIgnoreCase(value);
        }
        return true;
    }
}
