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
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static be.garagepoort.mcioc.configuration.PropertyInjector.injectConfigurationProperties;

public class IocContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IocContainer.class);
    private final Map<Class, Object> beans = new HashMap<>();
    private final IocConditionalPropertyFilter iocConditionalPropertyFilter = new IocConditionalPropertyFilter();
    private final IocConditionalFilter iocConditionalFilter = new IocConditionalFilter();
    private Reflections reflections;
    private Map<String, FileConfiguration> configs;

    public void init(JavaPlugin javaPlugin, Map<String, FileConfiguration> configs) {
        reflections = new Reflections(javaPlugin.getClass().getPackage().getName(), new TypeAnnotationsScanner(), new SubTypesScanner());
        this.configs = configs;
        loadIocBeans(configs);
        loadCommandHandlerBeans(javaPlugin);
        loadListenerBeans(javaPlugin);
        loadMessageListenerBeans(javaPlugin);
    }

    private void loadIocBeans(Map<String, FileConfiguration> configs) {
        try {
            Set<Class<?>> configurationClasses = reflections.getTypesAnnotatedWith(TubingConfiguration.class);
            List<Method> providers = configurationClasses.stream().flatMap(c -> ReflectionUtils.getMethodsAnnotatedWith(c, IocBeanProvider.class).stream()).collect(Collectors.toList());
            List<Method> multiProviders = configurationClasses.stream().flatMap(c -> ReflectionUtils.getMethodsAnnotatedWith(c, IocMultiProvider.class).stream()).collect(Collectors.toList());
            List<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(IocBean.class).stream()
                    .filter(a -> iocConditionalPropertyFilter.isValidBean(a, configs))
                    .filter(iocConditionalFilter::isValidBean)
                    .sorted((o1, o2) -> {
                        IocBean annotation1 = o1.getAnnotation(IocBean.class);
                        IocBean annotation2 = o2.getAnnotation(IocBean.class);
                        return Boolean.compare(annotation2.priority(), annotation1.priority());
                    })
                    .collect(Collectors.toList());

            Set<Class<?>> providedBeans = providers.stream().map(Method::getReturnType).collect(Collectors.toCollection(LinkedHashSet::new));
            Set<Class<?>> validBeans = Stream.concat(typesAnnotatedWith.stream(), providedBeans.stream()).collect(Collectors.toCollection(LinkedHashSet::new));
            for (Class<?> aClass : validBeans) {
                instantiateBean(reflections, aClass, validBeans, providers, multiProviders, false);
            }

            List<Method> afterMethods = configurationClasses.stream().flatMap(c -> ReflectionUtils.getMethodsAnnotatedWith(c, AfterIocLoad.class).stream()).collect(Collectors.toList());
            for (Method afterMethod : afterMethods) {
                List<Object> params = buildParams(reflections, validBeans, providers, multiProviders, afterMethod.getParameterTypes(), afterMethod.getParameterAnnotations());
                afterMethod.invoke(null, params.toArray());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IocException("Could not validate instantiate beans", e);
        }
    }

    private void loadCommandHandlerBeans(JavaPlugin javaPlugin) {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(IocCommandHandler.class);

        for (Class<?> aClass : typesAnnotatedWith) {
            if (!CommandExecutor.class.isAssignableFrom(aClass)) {
                throw new IocException("IocCommandHandler annotation can only be used on CommandExecutors");
            }
            if (!beans.containsKey(aClass)) {
                continue;
            }
            CommandExecutor bean = (CommandExecutor) this.get(aClass);
            IocCommandHandler annotation = aClass.getAnnotation(IocCommandHandler.class);
            javaPlugin.getCommand(annotation.value()).setExecutor(bean);
        }
    }

    private void loadListenerBeans(JavaPlugin javaPlugin) {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(IocListener.class);

        for (Class<?> aClass : typesAnnotatedWith) {
            if (!Listener.class.isAssignableFrom(aClass)) {
                throw new IocException("IocListener annotation can only be used on bukkit Listeners");
            }
            if (!beans.containsKey(aClass)) {
                continue;
            }
            Listener bean = (Listener) this.get(aClass);
            Bukkit.getPluginManager().registerEvents(bean, javaPlugin);
        }
    }


    private void loadMessageListenerBeans(JavaPlugin javaPlugin) {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(IocMessageListener.class);

        for (Class<?> aClass : typesAnnotatedWith) {
            if (!PluginMessageListener.class.isAssignableFrom(aClass)) {
                throw new IocException("IocMessageListener annotation can only be used on bukkit PluginMessageListeners");
            }
            if (!beans.containsKey(aClass)) {
                continue;
            }
            PluginMessageListener bean = (PluginMessageListener) this.get(aClass);
            IocMessageListener annotation = aClass.getAnnotation(IocMessageListener.class);
            javaPlugin.getServer().getMessenger().registerIncomingPluginChannel(javaPlugin, annotation.channel(), bean);
        }
    }

    private Object instantiateBean(Reflections reflections, Class<?> aClass, Set<Class<?>> validBeans, List<Method> providedBeans, List<Method> multiProviders, boolean multiProvider) throws InvocationTargetException, IllegalAccessException {
        LOGGER.debug("[MC-IOC] Instantiating bean [{}]", aClass.getName());

        if (multiProvider) {
            beans.putIfAbsent(aClass, new ArrayList<>());
            Set<Class<?>> subTypesOf = reflections.getSubTypesOf((Class<Object>) aClass).stream().filter(validBeans::contains).collect(Collectors.toSet());
            List list = (List) beans.get(aClass);
            for (Class<?> subClass : subTypesOf) {
                Object bean = createBean(reflections, subClass, validBeans, providedBeans, multiProviders);
                if (!list.contains(bean)) {
                    list.add(bean);
                }
            }

            Collection<Object> beans = getMultiProvidedBeans(reflections, aClass, validBeans, providedBeans, multiProviders);
            list.addAll(beans);

            return this.beans.get(aClass);
        }

        if (aClass.isAnnotationPresent(IocMultiProvider.class)) {
            Class multiClass = aClass.getAnnotation(IocMultiProvider.class).value();
            beans.putIfAbsent(multiClass, new ArrayList<>());
            List list = (List) beans.get(multiClass);
            Object bean = createBean(reflections, aClass, validBeans, providedBeans, multiProviders);
            if (!list.contains(bean) && bean != null) {
                list.add(bean);
            }
            return bean;
        }

        if (aClass.isInterface()) {
            Optional<Object> existingBean = beans.keySet().stream().filter(aClass::isAssignableFrom).map(beans::get).findFirst();
            if (existingBean.isPresent()) {
                return existingBean.get();
            }

            // Check if provider can handle it
            List<Method> currentProviders = providedBeans.stream().filter(p -> p.getReturnType() == aClass).collect(Collectors.toList());
            if (currentProviders.size() > 1) {
                throw new IocException("Multiple bean providers found for interface " + aClass.getName() + ". This is currently not supported");
            }
            if (currentProviders.size() == 1) {
                return createBean(reflections, aClass, validBeans, providedBeans, multiProviders);
            }

            // Find only implementation of interface and instantiate
            Set<Class<?>> subTypesOf = reflections.getSubTypesOf((Class<Object>) aClass).stream().filter(validBeans::contains).collect(Collectors.toSet());
            if (subTypesOf.isEmpty()) {
                throw new IocException("Cannot instantiate bean with interface " + aClass.getName() + ". No classes implementing this interface");
            }
            if (subTypesOf.size() > 1) {
                throw new IocException("Multiple beans found with interface " + aClass.getName() + ". At most one bean should be defined. Use @IocMultiProvider for supporting multiple beans with one interface");
            }
            return createBean(reflections, subTypesOf.iterator().next(), validBeans, providedBeans, multiProviders);
        }
        return createBean(reflections, aClass, validBeans, providedBeans, multiProviders);
    }

    private Object createBean(Reflections reflections, Class<?> aClass, Set<Class<?>> validBeans, List<Method> providers, List<Method> multiProviders) throws InvocationTargetException, IllegalAccessException {
        if (beans.containsKey(aClass)) {
            return beans.get(aClass);
        }

        Optional<Method> beanProvider = providers.stream().filter(p -> p.getReturnType() == aClass).findFirst();
        if (beanProvider.isPresent()) {
            return getProvidedBean(reflections, aClass, validBeans, providers, multiProviders);
        }

        if (!aClass.isAnnotationPresent(IocBean.class) && providers.stream().map(Method::getReturnType).noneMatch(a -> a == aClass)) {
            throw new IocException("Cannot instantiate bean. No IocBean annotation present. [" + aClass.getName() + "]");
        }

        Constructor<?>[] declaredConstructors = aClass.getDeclaredConstructors();
        if (declaredConstructors.length > 1) {
            throw new IocException("Cannot instantiate bean with type " + aClass.getName() + ". Only one constructor should be defined");
        }

        LOGGER.debug("[MC-IOC] Start creation of bean [{}]", aClass.getName());
        Constructor<?> declaredConstructor = aClass.getDeclaredConstructors()[0];
        List<Object> constructorParams = buildParams(reflections, validBeans, providers, multiProviders, declaredConstructor.getParameterTypes(), declaredConstructor.getParameterAnnotations());

        try {
            LOGGER.debug("[MC-IOC] Creating new bean [{}] with constructor arguments [{}]", aClass.getName(), constructorParams.stream().map(d -> d.getClass().getName()).collect(Collectors.joining(",")));
            Object bean = declaredConstructor.newInstance(constructorParams.toArray());
            injectConfigurationProperties(bean, configs);
            beans.putIfAbsent(aClass, bean);
            return bean;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IocException("Cannot instantiate bean with type " + aClass.getName() + ".", e);
        }
    }

    private Object getProvidedBean(Reflections reflections, Class<?> aClass, Set<Class<?>> validBeans, List<Method> providers, List<Method> multiProviders) throws InvocationTargetException, IllegalAccessException {
        Optional<Method> beanProvider = providers.stream().filter(p -> p.getReturnType() == aClass).findFirst();
        if (beanProvider.isPresent()) {
            List<Object> params = buildParams(reflections, validBeans, providers, multiProviders, beanProvider.get().getParameterTypes(), beanProvider.get().getParameterAnnotations());
            Object invoke = beanProvider.get().invoke(null, params.toArray());
            if (invoke != null) {
                beans.putIfAbsent(beanProvider.get().getReturnType(), invoke);
                return invoke;
            }
        }
        return null;
    }

    private Collection<Object> getMultiProvidedBeans(Reflections reflections, Class<?> aClass, Set<Class<?>> validBeans, List<Method> providers, List<Method> multiProviders) throws InvocationTargetException, IllegalAccessException {
        Optional<Method> beanProvider = multiProviders.stream().filter(p -> p.getAnnotation(IocMultiProvider.class).value().equals(aClass)).findFirst();
        if (beanProvider.isPresent()) {
            List<Object> params = buildParams(reflections, validBeans, providers, multiProviders, beanProvider.get().getParameterTypes(), beanProvider.get().getParameterAnnotations());
            Collection invoke = (Collection) beanProvider.get().invoke(null, params.toArray());
            multiProviders.remove(beanProvider.get());
            return invoke == null ? Collections.emptyList() : invoke;
        }
        return Collections.emptyList();
    }

    private List<Object> buildParams(Reflections reflections, Set<Class<?>> validBeans, List<Method> providedBeans, List<Method> multiProviders, Class<?>[] parameterTypes, Annotation[][] parameterAnnotations) throws InvocationTargetException, IllegalAccessException {
        List<Object> constructorParams = new ArrayList<>();

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> classParam = parameterTypes[i];
            Annotation[] annotations = parameterAnnotations[i];
            Optional<Annotation> multiAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(IocMulti.class)).findFirst();
            if (multiAnnotation.isPresent()) {
                IocMulti iocMulti = (IocMulti) multiAnnotation.get();
                constructorParams.add(instantiateBean(reflections, iocMulti.value(), validBeans, providedBeans, multiProviders, true));
            } else {
                Object o = instantiateBean(reflections, classParam, validBeans, providedBeans, multiProviders, false);
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
