package be.garagepoort.mcioc;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class IocContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IocContainer.class);
    private final Map<Class, Object> beans = new HashMap<>();
    private final IocConditionalPropertyFilter iocConditionalPropertyFilter = new IocConditionalPropertyFilter();

    public void init(JavaPlugin javaPlugin, FileConfiguration config) {
        Reflections reflections = new Reflections(javaPlugin.getClass().getPackage().getName(), new TypeAnnotationsScanner(), new SubTypesScanner());
        loadIocBeans(config, reflections);
        loadCommandHandlerBeans(javaPlugin, reflections);
        loadListenerBeans(javaPlugin, reflections);
        loadMessageListenerBeans(javaPlugin, reflections);
    }

    private void loadIocBeans(FileConfiguration config, Reflections reflections) {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(IocBean.class).stream()
                .filter(a -> iocConditionalPropertyFilter.isValidBean(a, config))
                .collect(Collectors.toSet());

        for (Class<?> aClass : typesAnnotatedWith) {
            instantiateBean(reflections, aClass, typesAnnotatedWith, false);
        }
    }

    private void loadCommandHandlerBeans(JavaPlugin javaPlugin, Reflections reflections) {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(IocCommandHandler.class);

        for (Class<?> aClass : typesAnnotatedWith) {
            if(!CommandExecutor.class.isAssignableFrom(aClass)) {
                throw new IocException("IocCommandHandler annotation can only be used on CommandExecutors");
            }
            if(!beans.containsKey(aClass)) {
                continue;
            }
            CommandExecutor bean = (CommandExecutor) this.get(aClass);
            IocCommandHandler annotation = aClass.getAnnotation(IocCommandHandler.class);
            javaPlugin.getCommand(annotation.value()).setExecutor(bean);
        }
    }

    private void loadListenerBeans(JavaPlugin javaPlugin, Reflections reflections) {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(IocListener.class);

        for (Class<?> aClass : typesAnnotatedWith) {
            if(!Listener.class.isAssignableFrom(aClass)) {
                throw new IocException("IocListener annotation can only be used on bukkit Listeners");
            }
            if(!beans.containsKey(aClass)) {
                continue;
            }
            Listener bean = (Listener) this.get(aClass);
            Bukkit.getPluginManager().registerEvents(bean, javaPlugin);
        }
    }


    private void loadMessageListenerBeans(JavaPlugin javaPlugin, Reflections reflections) {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(IocMessageListener.class);

        for (Class<?> aClass : typesAnnotatedWith) {
            if(!PluginMessageListener.class.isAssignableFrom(aClass)) {
                throw new IocException("IocMessageListener annotation can only be used on bukkit PluginMessageListeners");
            }
            if(!beans.containsKey(aClass)) {
                continue;
            }
            PluginMessageListener bean = (PluginMessageListener) this.get(aClass);
            IocMessageListener annotation = aClass.getAnnotation(IocMessageListener.class);
            javaPlugin.getServer().getMessenger().registerIncomingPluginChannel(javaPlugin, annotation.channel(), bean);
        }
    }

    private Object instantiateBean(Reflections reflections, Class<?> aClass, Set<Class<?>> validBeans, boolean multiProvider) {
        LOGGER.debug("[MC-IOC] Instantiating bean [{}]", aClass.getName());

        if (multiProvider) {
            beans.putIfAbsent(aClass, new ArrayList<>());
            Set<Class<?>> subTypesOf = reflections.getSubTypesOf((Class<Object>) aClass).stream().filter(validBeans::contains).collect(Collectors.toSet());
            List list = (List) beans.get(aClass);
            for (Class<?> subClass : subTypesOf) {
                Object bean = createBean(reflections, subClass, validBeans);
                if (!list.contains(bean)) {
                    list.add(bean);
                }
            }
            return beans.get(aClass);
        }

        if (aClass.isAnnotationPresent(IocMultiProvider.class)) {
            Class multiClass = aClass.getAnnotation(IocMultiProvider.class).value();
            beans.putIfAbsent(multiClass, new ArrayList<>());
            List list = (List) beans.get(multiClass);
            Object bean = createBean(reflections, aClass, validBeans);
            if (!list.contains(bean)) {
                list.add(bean);
            }
            return bean;
        }

        if (aClass.isInterface()) {
            Optional<Object> existingBean = beans.keySet().stream().filter(aClass::isAssignableFrom).map(beans::get).findFirst();
            if (existingBean.isPresent()) {
                return existingBean.get();
            }

            Set<Class<?>> subTypesOf = reflections.getSubTypesOf((Class<Object>) aClass).stream().filter(validBeans::contains).collect(Collectors.toSet());
            if (subTypesOf.isEmpty()) {
                throw new IocException("Cannot instantiate bean with interface " + aClass.getName() + ". No classes implementing this interface");
            }
            if (subTypesOf.size() > 1) {
                throw new IocException("Multiple beans found with interface " + aClass.getName() + ". At most one bean should be defined. Use @IocMultiProvider for supporting multiple beans with one interface");
            }
            return createBean(reflections, subTypesOf.iterator().next(), validBeans);
        }
        return createBean(reflections, aClass, validBeans);
    }

    private Object createBean(Reflections reflections, Class<?> aClass, Set<Class<?>> validBeans) {
        if (beans.containsKey(aClass)) {
            return beans.get(aClass);
        }
        if (!aClass.isAnnotationPresent(IocBean.class)) {
            throw new IocException("Cannot instantiate bean. No IocBean annotation present. [" + aClass.getName() + "]");
        }
        Constructor<?>[] declaredConstructors = aClass.getDeclaredConstructors();
        if (declaredConstructors.length > 1) {
            throw new IocException("Cannot instantiate bean with type " + aClass.getName() + ". Only one constructor should be defined");
        }

        LOGGER.debug("[MC-IOC] Start creation of bean [{}]", aClass.getName());
        Constructor<?> declaredConstructor = aClass.getDeclaredConstructors()[0];
        List<Object> constructorParams = buildConstructorParams(reflections, validBeans, declaredConstructor);

        try {
            LOGGER.debug("[MC-IOC] Creating new bean [{}] with constructor arguments [{}]", aClass.getName(), constructorParams.stream().map(d -> d.getClass().getName()).collect(Collectors.joining(",")));
            Object bean = declaredConstructor.newInstance(constructorParams.toArray());
            beans.putIfAbsent(aClass, bean);
            return bean;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IocException("Cannot instantiate bean with type " + aClass.getName() + ".", e);
        }
    }

    private List<Object> buildConstructorParams(Reflections reflections, Set<Class<?>> validBeans, Constructor<?> declaredConstructor) {
        List<Object> constructorParams = new ArrayList<>();

        Class<?>[] parameterTypes = declaredConstructor.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> classParam = parameterTypes[i];
            Annotation[] parameterAnnotations = declaredConstructor.getParameterAnnotations()[i];
            Optional<Annotation> multiAnnotation = Arrays.stream(parameterAnnotations).filter(a -> a.annotationType().equals(IocMulti.class)).findFirst();
            if (multiAnnotation.isPresent()) {
                IocMulti iocMulti = (IocMulti) multiAnnotation.get();
                Object o = instantiateBean(reflections, iocMulti.value(), validBeans, true);
                constructorParams.add(o);
            } else {
                Object o = instantiateBean(reflections, classParam, validBeans, false);
                constructorParams.add(o);
            }
        }
        return constructorParams;
    }

    public void registerBean(Object o) {
        beans.put(o.getClass(), o);
    }

    public <T> T get(Class<T> clazz) {
        if (clazz.isInterface()) {
            List<Object> collect = beans.keySet().stream().filter(clazz::isAssignableFrom).map(beans::get).collect(Collectors.toList());
            if (collect.size() > 1) {
                throw new IocException("Cannot retrieve bean with interface " + clazz.getName() + ". Too many implementations registered. Use `getList` to retrieve a list of all beans");
            }
            if (collect.isEmpty()) {
                throw new IocException("Cannot retrieve bean with interface " + clazz.getName() + ". No implementation registered");
            }
            return (T) collect.get(0);
        }
        return (T) beans.get(clazz);
    }

    public <T> List<T> getList(Class<T> clazz) {
        return (List<T>) beans.get(clazz);
    }

}
