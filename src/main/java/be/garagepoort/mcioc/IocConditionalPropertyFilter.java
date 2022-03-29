package be.garagepoort.mcioc;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IocConditionalPropertyFilter {

    public boolean isValidBean(List<Class> beanAnnotations, Class clazz, Map<String, FileConfiguration> configs) {
        try {
            Annotation annotation = Arrays.stream(clazz.getAnnotations()).filter(a -> beanAnnotations.contains(a.annotationType())).findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid Tubing configuration. No bean annotation on class: " + clazz.getName()));
            String conditionalOnProperty = (String) annotation.annotationType().getMethod("conditionalOnProperty").invoke(annotation);

            if (!StringUtils.isEmpty(conditionalOnProperty)) {
                List<String> conditionSections = Arrays.stream(conditionalOnProperty.split("&&")).map(String::trim).collect(Collectors.toList());
                return conditionSections.stream().allMatch(c -> isValid(configs, c));
            }
            return true;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Invalid bean configuration. not property found");
        }
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
