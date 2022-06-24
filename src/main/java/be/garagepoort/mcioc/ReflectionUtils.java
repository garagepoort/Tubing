package be.garagepoort.mcioc;

import be.garagepoort.mcioc.configuration.config.Configuration;

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

    public static <T> Optional<T> getConfigValue(String identifier, Map<String, Configuration> configs) {

        identifier = replaceNestedValues(identifier, configs);

        String configFileId = "config";
        String path = identifier;

        String[] fileSelectors = identifier.split(":", 2);
        if (fileSelectors.length == 2) {
            configFileId = fileSelectors[0];
            path = fileSelectors[1];
        }
        return Optional.ofNullable((T) configs.get(configFileId).get(path));
    }

    public static Optional<String> getConfigStringValue(String identifier, Map<String, Configuration> configs) {
        identifier = replaceNestedValues(identifier, configs);
        String configFileId = "config";
        String path = identifier;

        String[] fileSelectors = identifier.split(":", 2);
        if (fileSelectors.length == 2) {
            configFileId = fileSelectors[0];
            path = fileSelectors[1];
        }
        return Optional.ofNullable(configs.get(configFileId).getString(path));
    }

    private static String replaceNestedValues(String identifier, Map<String, Configuration> configs) {
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
