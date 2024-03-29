package be.garagepoort.mcioc.configuration.transformers;

import be.garagepoort.mcioc.configuration.PropertyInjector;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;
import be.garagepoort.mcioc.configuration.yaml.configuration.MemorySection;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;

public class ConfigEmbeddedObjectTransformer {

    public static <T> T transform(Class<T> objectClass, LinkedHashMap<String, Object> map) {
            try {
                T instance = (T) objectClass.getConstructor().newInstance();
                PropertyInjector.injectEmbeddedConfigurationProperties(instance, map);
                return instance;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new ConfigurationException("Invalid ConfigEmbeddedObject configuration");
            }
    }
    public static <T> T transform(Class<T> objectClass, MemorySection section) {
        try {
            T instance = (T) objectClass.getConstructor().newInstance();
            PropertyInjector.injectEmbeddedConfigurationProperties(instance, section);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ConfigurationException("Invalid ConfigEmbeddedObject configuration");
        }
    }
}
