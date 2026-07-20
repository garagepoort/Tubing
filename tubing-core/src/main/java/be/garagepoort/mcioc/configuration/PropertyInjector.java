package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.configuration.transformers.ConfigEmbeddedObjectTransformer;
import be.garagepoort.mcioc.configuration.transformers.ConfigObjectListTransformer;
import be.garagepoort.mcioc.configuration.yaml.configuration.MemorySection;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;
import be.garagepoort.mcioc.diagnostics.TubingDiagnosticException;

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

        ConfigProperty annotation = configAnnotation.get();
        String configProperty = getConfigProperty(configProperties, annotation);
        Optional<Object> configValue = parseConfig(classParam,
                annotation,
                configTransformerAnnotation.orElse(null),
                configObjectListAnnotation.orElse(null),
                configEmbeddedObject.orElse(null),
                new ConfigInjectionContext(aClass, "constructor parameter " + classParam.getName(), classParam, configProperty, configTransformerAnnotation.orElse(null)),
                v -> ReflectionUtils.getConfigValue(v, configs));
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
                ConfigProperty annotation = configMethod.getAnnotation(ConfigProperty.class);
                Class<?> parameterType = configMethod.getParameterTypes()[0];
                Optional<Object> parsedConfigValue = parseConfig(parameterType, annotation, null, null, null,
                        new ConfigInjectionContext(o.getClass(), "method " + configMethod.getName(), parameterType, getConfigProperty(configProperties, annotation), null),
                        configRetrievalFunction);
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
                Optional<Object> parsedConfigValue = parseConfig(f.getType(), annotation, configTransformer, configObjectListAnnotation, configEmbeddedObject,
                        new ConfigInjectionContext(o.getClass(), "field " + f.getName(), f.getType(), getConfigProperty(configProperties, annotation), configTransformer),
                        configRetrievalFunction);
                if (parsedConfigValue.isPresent()) {
                    f.setAccessible(true);
                    f.set(o, parsedConfigValue.get());
                }
            }
        } catch (TubingDiagnosticException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new IocException("Cannot inject property. Make sure the field is public", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof TubingDiagnosticException) {
                throw (TubingDiagnosticException) cause;
            }
            throw new IocException("Cannot inject property. Make sure the config property setter is public", e);
        }
    }

    private static <T> Optional<T> parseConfig(Class type,
                                               ConfigProperty configAnnotation,
                                               ConfigTransformer configTransformer,
                                               ConfigObjectList configObjectList,
                                               ConfigEmbeddedObject configEmbeddedObject,
                                               ConfigInjectionContext context,
                                               Function<String, Optional> configRetrievalFunction) {
        String configProperty = context.getProperty();

        try {
            Optional configValue = configRetrievalFunction.apply(configProperty);
            if (!configValue.isPresent()) {
                if (configAnnotation.required()) {
                    throw context.missingRequired(configAnnotation.error());
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
                    try {
                        transformedConfig = iConfigTransformer.mapConfig(transformedConfig);
                    } catch (RuntimeException e) {
                        throw context.transformerFailed(transformerClass, transformedConfig, e);
                    }
                }
                return (Optional<T>) Optional.ofNullable(transformedConfig);
            } else {
                Object value = configValue.get();
                Optional<T> convertedValue = convertValue(type, value);
                if (!convertedValue.isPresent()) {
                    throw context.conversionFailed(value, null);
                }
                return convertedValue;
            }
        } catch (TubingDiagnosticException e) {
            throw e;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IocException("Cannot create configtransformer", e);
        } catch (ClassCastException e) {
            throw context.conversionFailed(configRetrievalFunction.apply(configProperty).orElse(null), e);
        }
    }

    private static <T> Optional<T> convertValue(Class type, Object value) {
        if (value == null) {
            return Optional.ofNullable(null);
        }
        if (type.isAssignableFrom(value.getClass())) {
            return Optional.of((T) value);
        }
        if (type == boolean.class || type == Boolean.class) {
            return value instanceof Boolean ? Optional.of((T) value) : Optional.empty();
        }
        if (type == byte.class || type == Byte.class) {
            return value instanceof Number ? Optional.of((T) Byte.valueOf(((Number) value).byteValue())) : Optional.empty();
        }
        if (type == short.class || type == Short.class) {
            return value instanceof Number ? Optional.of((T) Short.valueOf(((Number) value).shortValue())) : Optional.empty();
        }
        if (type == int.class || type == Integer.class) {
            return value instanceof Number ? Optional.of((T) Integer.valueOf(((Number) value).intValue())) : Optional.empty();
        }
        if (type == long.class || type == Long.class) {
            return value instanceof Number ? Optional.of((T) Long.valueOf(((Number) value).longValue())) : Optional.empty();
        }
        if (type == float.class || type == Float.class) {
            return value instanceof Number ? Optional.of((T) Float.valueOf(((Number) value).floatValue())) : Optional.empty();
        }
        if (type == double.class || type == Double.class) {
            return value instanceof Number ? Optional.of((T) Double.valueOf(((Number) value).doubleValue())) : Optional.empty();
        }
        if (type == char.class || type == Character.class) {
            if (value instanceof Character) {
                return Optional.of((T) value);
            }
            if (value instanceof String && ((String) value).length() == 1) {
                return Optional.of((T) Character.valueOf(((String) value).charAt(0)));
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static String getConfigProperty(ConfigProperties configProperties, ConfigProperty configAnnotation) {
        String prefix = "";
        if (configProperties != null) {
            prefix = configProperties.value() + ".";
        }
        return prefix + configAnnotation.value();
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
