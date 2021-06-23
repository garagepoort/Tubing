package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.ReflectionUtils;
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
            for (Field f : o.getClass().getDeclaredFields()) {
                if (!f.isAnnotationPresent(ConfigProperty.class)) {
                    continue;
                }
                ConfigProperty annotation = f.getAnnotation(ConfigProperty.class);
                if (f.isAnnotationPresent(ConfigListTransformer.class)) {
                    ConfigListTransformer listTransformer = f.getAnnotation(ConfigListTransformer.class);
                    Constructor<?> declaredConstructor = listTransformer.value().getDeclaredConstructors()[0];
                    IConfigListTransformer iConfigListTransformer = (IConfigListTransformer) declaredConstructor.newInstance();
                    f.setAccessible(true);
                    f.set(o, iConfigListTransformer.mapConfig(ReflectionUtils.getConfigListValue(annotation.value(), configs)));
                } else {
                    Optional<Object> configValue = ReflectionUtils.getConfigValue(annotation.value(), configs);
                    if (configValue.isPresent()) {
                        f.setAccessible(true);
                        f.set(o, configValue.get());
                    }
                }

            }

        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IocException("Cannot inject property. Make sure the field is public", e);
        }
    }
}
