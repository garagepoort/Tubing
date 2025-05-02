package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;
import be.garagepoort.mcioc.configuration.transformers.ConfigEmbeddedObjectTransformer;
import be.garagepoort.mcioc.configuration.transformers.ConfigObjectListTransformer;
import be.garagepoort.mcioc.configuration.yaml.configuration.MemorySection;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.lang.ClassCastException;
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

    public static void injectEmbeddedConfigurationProperties(Object bean, MemorySection configs) {
        setProperties((v) -> Optional.ofNullable(configs.get(v)), bean);
    }

    public static Object getConstructorConfigurationProperty(Class<?> aClass, Class<?> classParam, Annotation[] annotations, Map<String, FileConfiguration> configs) {
        ConfigProperties configProperties = null;
        if (aClass.isAnnotationPresent(ConfigProperties.class)) {
            configProperties = aClass.getAnnotation(ConfigProperties.class);
        }

        Optional<ConfigProperty> configAnnotation = Arrays.stream(annotations)
                .filter(a -> a.annotationType().equals(ConfigProperty.class))
                .map(a -> (ConfigProperty) a).findFirst();

        Optional<ConfigTransformer> configTransformerAnnotation = Arrays.stream(annotations)
                .filter(a -> a.annotationType().equals(ConfigTransformer.class))
                .map(a -> (ConfigTransformer) a).findFirst();

        Optional<ConfigObjectList> configObjectListAnnotation = Arrays.stream(annotations)
                .filter(a -> a.annotationType().equals(ConfigObjectList.class))
                .map(a -> (ConfigObjectList) a).findFirst();

        Optional<ConfigEmbeddedObject> configEmbeddedObject = Arrays.stream(annotations)
                .filter(a -> a.annotationType().equals(ConfigEmbeddedObject.class))
                .map(a -> (ConfigEmbeddedObject) a).findFirst();

        Optional<Object> configValue = parseConfig(classParam,
                configProperties,
                configAnnotation.get(),
                configTransformerAnnotation.orElse(null),
                configObjectListAnnotation.orElse(null),
                configEmbeddedObject.orElse(null), v -> ReflectionUtils.getConfigValue(v, configs));
        return configValue.orElse(null);
    }

    private static void setProperties(Function<String, Optional> configRetrievalFunction, Object o) {
        try {
            ConfigProperties configProperties = null;
            if (o.getClass().isAnnotationPresent(ConfigProperties.class)) {
                configProperties = o.getClass().getAnnotation(ConfigProperties.class);
            }

            List<Method> configMethods = ReflectionUtils.getMethodsAnnotatedWith(o.getClass(), ConfigProperty.class);
            for (Method configMethod : configMethods) {
                Optional<Object> parsedConfigValue = parseConfig(configMethod.getParameterTypes()[0], configProperties, configMethod.getAnnotation(ConfigProperty.class), null, null, null, configRetrievalFunction);
                if (parsedConfigValue.isPresent()) {
                    configMethod.invoke(o, parsedConfigValue.get());
                }
            }

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

                ConfigEmbeddedObject configEmbeddedObject = null;
                if (f.isAnnotationPresent(ConfigEmbeddedObject.class)) {
                    configEmbeddedObject = f.getAnnotation(ConfigEmbeddedObject.class);
                }

                ConfigProperty annotation = f.getAnnotation(ConfigProperty.class);
                Optional<Object> parsedConfigValue = parseConfig(f.getType(), configProperties, annotation, configTransformer, configObjectListAnnotation, configEmbeddedObject, configRetrievalFunction);
                if (parsedConfigValue.isPresent()) {
                    f.setAccessible(true);
                    f.set(o, parsedConfigValue.get());
                }
            }
        } catch (IllegalAccessException e) {
            throw new IocException("Cannot inject property. Make sure the field is public", e);
        } catch (InvocationTargetException e) {
            throw new IocException("Cannot inject property. Make sure the config property setter is public", e);
        }
    }

    private static <T> Optional<T> parseConfig(Class type,
                                               ConfigProperties configProperties,
                                               ConfigProperty configAnnotation,
                                               ConfigTransformer configTransformer,
                                               ConfigObjectList configObjectList,
                                               ConfigEmbeddedObject configEmbeddedObject,
                                               Function<String, Optional> configRetrievalFunction) {
        String prefix = "";
        if (configProperties != null) {
            prefix = configProperties.value() + ".";
        }
        String configProperty = prefix + configAnnotation.value();
        
        try {
            Optional configValue = configRetrievalFunction.apply(configProperty);
            if (!configValue.isPresent()) {
                if (configAnnotation.required()) {
                    throw new ConfigurationException(configAnnotation.error().isEmpty() ? "Configuration not found for " + configProperty : configAnnotation.error());
                }
                return Optional.empty();
            }

            if (configEmbeddedObject != null) {
                Class objectClass = configEmbeddedObject.value();
                Object configSection = configValue.get();
                if (configSection instanceof MemorySection) {
                    MemorySection section = (MemorySection) configSection;
                    return (Optional<T>) Optional.ofNullable(ConfigEmbeddedObjectTransformer.transform(objectClass, section));
                }
                if (configSection instanceof LinkedHashMap) {
                    LinkedHashMap<String, Object> listOfMaps = (LinkedHashMap<String, Object>) configValue.get();
                    return (Optional<T>) Optional.ofNullable(ConfigEmbeddedObjectTransformer.transform(objectClass, listOfMaps));
                }
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
        } catch (ClassCastException e) {
            throw new ConfigurationException("Failed to convert configuration value for '" + configProperty + "', is it correct type?", e);
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
