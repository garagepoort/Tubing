package be.garagepoort.mcioc;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IocConditionalPropertyFilter {

    public boolean isValidBean(Class a, Map<String, FileConfiguration> configs) {
        IocBean annotation = (IocBean) a.getAnnotation(IocBean.class);
        String conditionalOnProperty = annotation.conditionalOnProperty();
        if (!StringUtils.isEmpty(conditionalOnProperty)) {
            List<String> conditionSections = Arrays.stream(conditionalOnProperty.split("&&")).map(String::trim).collect(Collectors.toList());

            return conditionSections.stream().allMatch(c -> isValid(configs, c));
        }
        return true;
    }

    private boolean isValid(Map<String, FileConfiguration> configs, String conditionalOnProperty) {
        if (conditionalOnProperty.startsWith("isNotEmpty")) {
            String key = StringUtils.substringBetween(conditionalOnProperty, "(", ")");

            String configValue = ReflectionUtils.getConfigStringValue(key, configs)
                    .orElseThrow(() -> new IocException("ConditionOnProperty referencing an unknown property [" + key + "]"));
            return StringUtils.isNotEmpty(configValue);
        } else {
            String[] split = conditionalOnProperty.split("=", 2);
            String key = split[0];
            String value = split[1];

            String configValue = ReflectionUtils.getConfigStringValue(key, configs)
                    .orElseThrow(() -> new IocException("ConditionOnProperty referencing an unknown property [" + key + "]"));

            return configValue.equalsIgnoreCase(value);
        }
    }
}
