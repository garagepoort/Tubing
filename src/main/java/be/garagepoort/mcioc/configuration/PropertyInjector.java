package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.TubingPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PropertyInjector {
    public static void injectConfigurationProperties(Object bean, Map<String, FileConfiguration> configs) {
        setProperties(configs, bean);
    }

    private static void setProperties(Map<String, FileConfiguration> configs, Object o) {
        try {
            for (Field f : getAllFields(new LinkedList<>(), o.getClass())) {
                if (!f.isAnnotationPresent(ConfigProperty.class)) {
                    continue;
                }
                ConfigTransformer configTransformer = null;
                if (f.isAnnotationPresent(ConfigTransformer.class)) {
                    configTransformer = f.getAnnotation(ConfigTransformer.class);
                }
                ConfigProperty annotation = f.getAnnotation(ConfigProperty.class);
                Optional<Object> parsedConfigValue = parseConfig(annotation, configTransformer, configs);
                if (parsedConfigValue.isPresent()) {
                    f.setAccessible(true);
                    f.set(o, parsedConfigValue.get());
                }
            }
        } catch (IllegalAccessException e) {
            throw new IocException("Cannot inject property. Make sure the field is public", e);
        }
    }

    public static Optional<Object> parseConfig(ConfigProperty configAnnotation, ConfigTransformer configTransformer, Map<String, FileConfiguration> configs) {
        try {
            Optional<Object> configValue = ReflectionUtils.getConfigValue(configAnnotation.value(), configs);
            if (!configValue.isPresent()) {
                TubingPlugin.getPlugin().getLogger().warning("[Tubing] >> No property found for config: " + configAnnotation.value());
                return Optional.empty();
            }
            if (configTransformer != null) {
                Constructor<?> declaredConstructor = configTransformer.value().getDeclaredConstructors()[0];
                IConfigTransformer iConfigTransformer = (IConfigTransformer) declaredConstructor.newInstance();
                setProperties(configs, iConfigTransformer);
                return Optional.ofNullable(iConfigTransformer.mapConfig(configValue.get()));
            } else {
                return configValue;
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IocException("Cannot create configtransformer", e);
        }
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    public static Enum getInstance(final String value, final Class enumClass) {
        return Enum.valueOf(enumClass, value);
    }
}
