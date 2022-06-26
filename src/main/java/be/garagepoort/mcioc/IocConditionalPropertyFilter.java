package be.garagepoort.mcioc;

import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;

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

            if (!isEmpty(conditionalOnProperty)) {
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
            String key = substringBetween(conditionalOnProperty, "(", ")");

            String configValue = ReflectionUtils.getConfigStringValue(key, configs)
                .orElseThrow(() -> new IocException("ConditionOnProperty referencing an unknown property [" + key + "]"));
            return isNotEmpty(configValue);
        } else if (conditionalOnProperty.startsWith("isEmpty")) {
            String key = substringBetween(conditionalOnProperty, "(", ")");

            String configValue = ReflectionUtils.getConfigStringValue(key, configs)
                .orElseThrow(() -> new IocException("ConditionOnProperty referencing an unknown property [" + key + "]"));
            return isBlank(configValue);
        } else {
            String[] split = conditionalOnProperty.split("=", 2);
            String key = split[0];
            String value = split[1];

            String configValue = ReflectionUtils.getConfigStringValue(key, configs)
                .orElseThrow(() -> new IocException("ConditionOnProperty referencing an unknown property [" + key + "]"));

            return configValue.equalsIgnoreCase(value);
        }
    }

    private String substringBetween(String str, String open, String close) {
        if (str != null && open != null && close != null) {
            int start = str.indexOf(open);
            if (start != -1) {
                int end = str.indexOf(close, start + open.length());
                if (end != -1) {
                    return str.substring(start + open.length(), end);
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    private boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    private boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }


}
