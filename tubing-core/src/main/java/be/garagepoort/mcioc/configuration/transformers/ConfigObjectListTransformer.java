package be.garagepoort.mcioc.configuration.transformers;

import be.garagepoort.mcioc.configuration.PropertyInjector;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigObjectListTransformer {

    public static <T> List<T> transform(Class<T> objectClass, List<LinkedHashMap<String, Object>> listOfMaps) {
        return Objects.requireNonNull(listOfMaps).stream().map(map -> {
            try {
                T instance = (T) objectClass.getConstructors()[0].newInstance();
                PropertyInjector.injectConfigurationPropertiesBla(instance, map);
                return instance;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ConfigurationException("Invalid ConfigObjectList configuration");
            }
        }).collect(Collectors.toList());
    }
}
