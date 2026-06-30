package be.garagepoort.mcioc;

import be.garagepoort.mcioc.configuration.ConfigKey;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionUtils {

    public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation>... annotations) {
        final List<Method> methods = new ArrayList<>();
        Class<?> klass = type;
        while (klass != Object.class) {
            for (final Method method : klass.getDeclaredMethods()) {
                if (Arrays.stream(annotations).allMatch(method::isAnnotationPresent)) {
                    methods.add(method);
                }
            }
            klass = klass.getSuperclass();
        }
        return methods;
    }

    public static <T> Optional<T> getConfigValue(String identifier, Map<String, FileConfiguration> configs) {

        identifier = replaceNestedValues(identifier, configs);

        ConfigKey configKey = ConfigKey.parse(identifier);
        configKey.requireConfiguration(configs);
        return (Optional<T>) configKey.getValue(configs);
    }

    public static Optional<String> getConfigStringValue(String identifier, Map<String, FileConfiguration> configs) {
        identifier = replaceNestedValues(identifier, configs);
        ConfigKey configKey = ConfigKey.parse(identifier);
        configKey.requireConfiguration(configs);
        return configKey.getStringValue(configs);
    }

    private static String replaceNestedValues(String identifier, Map<String, FileConfiguration> configs) {
        String regexString = Pattern.quote("%") + "(.*?)" + Pattern.quote("%");
        Pattern pattern = Pattern.compile(regexString);
        Matcher matcher = pattern.matcher(identifier);
        while (matcher.find()) {
            String nestedConfig = matcher.group(1);
            Optional<String> configValue = getConfigValue(nestedConfig, configs);
            if(configValue.isPresent()) {
                identifier = identifier.replace("%" + nestedConfig + "%", configValue.get());
            }
        }
        return identifier;
    }

}
