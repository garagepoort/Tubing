package be.garagepoort.mcioc;

import org.bukkit.configuration.file.FileConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

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
        String configFileId = "config";
        String path = identifier;

        String[] fileSelectors = identifier.split(":", 2);
        if (fileSelectors.length == 2) {
            configFileId = fileSelectors[0];
            path = fileSelectors[1];
        }
        return Optional.ofNullable((T) configs.get(configFileId).get(path));
    }

    public static Optional<String> getConfigStringValue(String identifier, Map<String, FileConfiguration> configs) {
        String configFileId = "config";
        String path = identifier;

        String[] fileSelectors = identifier.split(":", 2);
        if (fileSelectors.length == 2) {
            configFileId = fileSelectors[0];
            path = fileSelectors[1];
        }
        return Optional.ofNullable(configs.get(configFileId).getString(path));
    }


    public static List<LinkedHashMap<String, Object>> getConfigListValue(String identifier, Map<String, FileConfiguration> configs) {
        String configFileId = "config";
        String path = identifier;

        String[] fileSelectors = identifier.split(":", 2);
        if (fileSelectors.length == 2) {
            configFileId = fileSelectors[0];
            path = fileSelectors[1];
        }
        return (List<LinkedHashMap<String, Object>>) configs.get(configFileId).getList(path, new ArrayList<>());
    }


    public static FileConfiguration getFileConfig(String identifier, Map<String, FileConfiguration> configs) {
        String configFileId = "config";
        String[] fileSelectors = identifier.split(":", 2);
        if (fileSelectors.length == 2) {
            configFileId = fileSelectors[0];
        }
        return configs.get(configFileId);
    }
}
