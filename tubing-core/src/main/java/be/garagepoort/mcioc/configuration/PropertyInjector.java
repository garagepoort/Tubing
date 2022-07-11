package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.configuration.transformers.ConfigObjectListTransformer;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PropertyInjector {
    public static void injectConfigurationProperties(Object bean, Map<String, FileConfiguration> configs) {
        setProperties(v -> ReflectionUtils.getConfigValue(v, configs), bean);
    }

    public static void injectEmbeddedConfigurationProperties(Object bean, Map<String, Object> configs) {
        setProperties((v) -> Optional.ofNullable(configs.get(v)), bean);
    }

    public static Object getConstructorConfigurationProperty(Class<?> classParam, Annotation[] annotations, Map<String, FileConfiguration> configs) {
        Optional<ConfigProperty> configAnnotation = Arrays.stream(annotations)
            .filter(a -> a.annotationType().equals(ConfigProperty.class))
            .map(a -> (ConfigProperty) a).findFirst();

        Optional<ConfigTransformer> configTransformerAnnotation = Arrays.stream(annotations)
            .filter(a -> a.annotationType().equals(ConfigTransformer.class))
            .map(a -> (ConfigTransformer) a).findFirst();

        Optional<ConfigObjectList> configObjectListAnnotation = Arrays.stream(annotations)
            .filter(a -> a.annotationType().equals(ConfigObjectList.class))
            .map(a -> (ConfigObjectList) a).findFirst();

        Optional<Object> configValue = parseConfig(classParam, configAnnotation.get(), configTransformerAnnotation.orElse(null), configObjectListAnnotation.orElse(null), v -> ReflectionUtils.getConfigValue(v, configs));
        return configValue.orElse(null);
    }

    private static void setProperties(Function<String, Optional> configRetrievalFunction, Object o) {
        try {
            for (Field f : getAllFields(new LinkedList<>(), o.getClass())) {
                if (!f.isAnnotationPresent(ConfigProperty.class)) {
                    continue;
                }
                ConfigTransformer configTransformer = null;
                if (f.isAnnotationPresent(ConfigTransformer.class)) {
                    configTransformer = f.getAnnotation(ConfigTransformer.class);
                }

                ConfigObjectList configObjectListAnnotation = null;
                if (f.isAnnotationPresent(ConfigObjectList.class)) {
                    configObjectListAnnotation = f.getAnnotation(ConfigObjectList.class);
                }

                ConfigProperty annotation = f.getAnnotation(ConfigProperty.class);
                Optional<Object> parsedConfigValue = parseConfig(f.getType(), annotation, configTransformer, configObjectListAnnotation, configRetrievalFunction);
                if (parsedConfigValue.isPresent()) {
                    f.setAccessible(true);
                    f.set(o, parsedConfigValue.get());
                }
            }
        } catch (IllegalAccessException e) {
            throw new IocException("Cannot inject property. Make sure the field is public", e);
        }
    }

    private static <T> Optional<T> parseConfig(Class type,
                                              ConfigProperty configAnnotation,
                                              ConfigTransformer configTransformer,
                                              ConfigObjectList configObjectList,
                                              Function<String, Optional> configRetrievalFunction) {
        try {
            Optional<T> configValue = configRetrievalFunction.apply(configAnnotation.value());
            if (!configValue.isPresent()) {
                return Optional.empty();
            }
            if (configObjectList != null) {
                Class objectClass = configObjectList.value();
                List<LinkedHashMap<String, Object>> listOfMaps = (List<LinkedHashMap<String, Object>>) configValue.get();
                return (Optional<T>) Optional.ofNullable(ConfigObjectListTransformer.transform(objectClass, listOfMaps));
            }
            if (configTransformer != null) {
                Object transformedConfig = configValue.get();
                for (Class<? extends IConfigTransformer> transformerClass : configTransformer.value()) {
                    Constructor<?> declaredConstructor = transformerClass.getDeclaredConstructors()[0];
                    IConfigTransformer iConfigTransformer;
                    Class<?>[] parameterTypes = declaredConstructor.getParameterTypes();
                    if (parameterTypes.length == 1) {
                        iConfigTransformer = (IConfigTransformer) declaredConstructor.newInstance(type);
                    } else if (parameterTypes.length == 0) {
                        iConfigTransformer = (IConfigTransformer) declaredConstructor.newInstance();
                    } else {
                        throw new IocException("Invalid IConfigTransformer. Invalid constructor");
                    }
                    setProperties(configRetrievalFunction, iConfigTransformer);
                    transformedConfig = iConfigTransformer.mapConfig(transformedConfig);
                }
                return (Optional<T>) Optional.ofNullable(transformedConfig);
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
}
