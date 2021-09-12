package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
                ConfigProperty annotation = f.getAnnotation(ConfigProperty.class);
                Optional<Object> configValue = ReflectionUtils.getConfigValue(annotation.value(), configs);
                if (!configValue.isPresent()) {
                    Bukkit.getLogger().warning("[Tubing] >> No property found for config: " + annotation.value());
                    continue;
                }
                if (f.isAnnotationPresent(ConfigTransformer.class)) {
                    ConfigTransformer configTransformer = f.getAnnotation(ConfigTransformer.class);
                    Constructor<?> declaredConstructor = configTransformer.value().getDeclaredConstructors()[0];
                    IConfigTransformer iConfigTransformer = (IConfigTransformer) declaredConstructor.newInstance();
                    f.setAccessible(true);
                    f.set(o, iConfigTransformer.mapConfig(configValue.get()));
                } else {
                        f.setAccessible(true);
                        f.set(o, configValue.get());
                }

            }

        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IocException("Cannot inject property. Make sure the field is public", e);
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
