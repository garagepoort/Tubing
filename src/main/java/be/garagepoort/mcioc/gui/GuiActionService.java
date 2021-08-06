package be.garagepoort.mcioc.gui;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.gui.exceptions.GuiExceptionHandler;
import be.garagepoort.mcioc.gui.templates.GuiTemplate;
import be.garagepoort.mcioc.gui.templates.GuiTemplateResolver;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@IocBean
public class GuiActionService {
    private final GuiTemplateResolver guiTemplateResolver;
    private final Map<String, Method> guiActions = new HashMap<>();
    private final Map<UUID, TubingGui> inventories = new HashMap<>();
    private final Map<Class<? extends Exception>, GuiExceptionHandler> exceptionHandlers = new HashMap<>();

    public GuiActionService(GuiTemplateResolver guiTemplateResolver) {
        this.guiTemplateResolver = guiTemplateResolver;
    }

    public void setInventory(Player player, TubingGui tubingGui) {
        inventories.put(player.getUniqueId(), tubingGui);
    }

    public Optional<TubingGui> getTubingGui(Player player) {
        return Optional.ofNullable(inventories.get(player.getUniqueId()));
    }

    public void registerExceptionHandler(Class<? extends Exception> clazz, GuiExceptionHandler guiExceptionHandler) {
        exceptionHandlers.put(clazz, guiExceptionHandler);
    }

    public void executeAction(Player player, String actionQuery) {
        try {
            String[] split = actionQuery.split(Pattern.quote("?"), 2);
            String action = split[0];

            if (!guiActions.containsKey(action)) {
                throw new IocException("No Gui Action found for [" + action + "]");
            }

            Method method = guiActions.get(action);
            Map<String, String> paramMap = getParams(split);
            Object[] methodParams = getMethodParams(method, paramMap, actionQuery, player);

            Object bean = TubingPlugin.getPlugin().getIocContainer().get(method.getDeclaringClass());
            if (bean == null) {
                throw new IocException("No GuiController found to handle action [" + actionQuery + "]. Tried finding [" + method.getClass() + "]");
            }

            try {
                Object invokedReturnedObject = method.invoke(bean, methodParams);
                if (invokedReturnedObject instanceof AsyncGui) {
                    processAsyncGuiAction(player, actionQuery, method, paramMap, (AsyncGui) invokedReturnedObject);
                } else {
                    processGuiAction(player, actionQuery, method, paramMap, invokedReturnedObject);
                }

            } catch (InvocationTargetException e) {
                handleException(player, e.getCause());
            }
        } catch (Throwable e) {
            throw new IocException("Unable to execute gui action", e);
        }
    }

    private void processAsyncGuiAction(Player player, String actionQuery, Method method, Map<String, String> paramMap, AsyncGui invokedReturnedObject) {
        Bukkit.getScheduler().runTaskAsynchronously(TubingPlugin.getPlugin(), () -> {
            try {
                AsyncGui asyncGui = invokedReturnedObject;
                Object run = asyncGui.getAsyncGuiExecutor().run();
                Bukkit.getScheduler().runTaskLater(TubingPlugin.getPlugin(), () -> processGuiAction(player, actionQuery, method, paramMap, run), 1);
            } catch (Throwable e) {
                try {
                    handleException(player, e);
                } catch (Throwable throwable) {
                    throw new IocException("Unable to execute gui action", e);
                }
            }
        });
    }

    private void handleException(Player player, Throwable e) throws Throwable {
        if (exceptionHandlers.containsKey(e.getClass())) {
            exceptionHandlers.get(e.getClass()).accept(player, e);
            player.closeInventory();
            removeInventory(player);
            return;
        }

        Optional<Class<? extends Exception>> parentException = exceptionHandlers.keySet().stream()
                .filter(c -> c.isAssignableFrom(e.getClass()))
                .findFirst();

        if (parentException.isPresent()) {
            exceptionHandlers.get(parentException.get()).accept(player, e);
            player.closeInventory();
            removeInventory(player);
            return;
        }

        throw e;
    }

    private void processGuiAction(Player player, String actionQuery, Method method, Map<String, String> paramMap, Object invokedReturnedObject) {
        if (invokedReturnedObject instanceof TubingGui) {
            TubingGui tubingGui = (TubingGui) invokedReturnedObject;
            showGui(player, tubingGui);
        } else if (invokedReturnedObject == null) {
            player.closeInventory();
            removeInventory(player);
        } else if (invokedReturnedObject instanceof GuiActionReturnType) {
            GuiActionReturnType actionReturnType = (GuiActionReturnType) invokedReturnedObject;
            if (actionReturnType != GuiActionReturnType.KEEP_OPEN) {
                player.closeInventory();
                removeInventory(player);
            }
        } else if (invokedReturnedObject instanceof GuiTemplate) {
            GuiTemplate guiTemplate = (GuiTemplate) invokedReturnedObject;
            Map<String, Object> templateParams = getTemplateParams(method, paramMap, actionQuery, player);
            templateParams.forEach((k, v) -> {
                if (!guiTemplate.getParams().containsKey(k)) {
                    guiTemplate.getParams().put(k, v);
                }
            });

            showGuiTemplate(player, guiTemplate);
        } else if (invokedReturnedObject instanceof String) {
            String redirectAction = (String) invokedReturnedObject;
            executeAction(player, redirectAction);
        } else {
            throw new IocException("Invalid returnType [" + invokedReturnedObject.getClass() + "] for GuiController [" + method.getDeclaringClass() + "]");
        }
    }

    public void showGui(Player player, TubingGui inventory) {
        Bukkit.getScheduler().runTaskLater(TubingPlugin.getPlugin(), () -> {
            player.closeInventory();
            player.openInventory(inventory.getInventory());
            setInventory(player, inventory);
        }, 1);
    }

    public void showGuiTemplate(Player player, GuiTemplate guiTemplate) {
        showGui(player, guiTemplateResolver.resolve(guiTemplate.getTemplate(), guiTemplate.getParams()));
    }

    private Object[] getMethodParams(Method method, Map<String, String> paramMap, String actionQuery, Player player) {
        Object[] methodParams = new Object[method.getParameterCount()];
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Optional<Annotation> paramAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(GuiParam.class)).findFirst();
            if (paramAnnotation.isPresent()) {
                GuiParam param = (GuiParam) paramAnnotation.get();
                if (paramMap.containsKey(param.value())) {
                    methodParams[i] = toObject(parameterTypes[i], URLDecoder.decode(paramMap.get(param.value())));
                } else if (StringUtils.isNotBlank(param.defaultValue())) {
                    methodParams[i] = toObject(parameterTypes[i], param.defaultValue());
                }
            } else if (parameterTypes[i] == Player.class) {
                methodParams[i] = player;
            } else {
                Optional<Annotation> currentActionAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(CurrentAction.class)).findFirst();
                if (currentActionAnnotation.isPresent()) {
                    methodParams[i] = actionQuery;
                }
            }
            Optional<Annotation> allParamsAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(GuiParams.class)).findFirst();
            if (allParamsAnnotation.isPresent()) {
                methodParams[i] = paramMap;
            }
        }
        return methodParams;
    }

    private Map<String, Object> getTemplateParams(Method method, Map<String, String> paramMap, String actionQuery, Player player) {
        Map<String, Object> methodParams = new HashMap<>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Optional<Annotation> paramAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(GuiParam.class)).findFirst();
            if (paramAnnotation.isPresent()) {
                GuiParam param = (GuiParam) paramAnnotation.get();
                if (paramMap.containsKey(param.value())) {
                    methodParams.put(param.value(), toObject(parameterTypes[i], URLDecoder.decode(paramMap.get(param.value()))));
                } else if (StringUtils.isNotBlank(param.defaultValue())) {
                    methodParams.put(param.value(), toObject(parameterTypes[i], param.defaultValue()));
                }
            }
        }
        methodParams.put("player", player);
        methodParams.put("currentAction", actionQuery);
        paramMap.forEach((k, v) -> {
            if (!methodParams.containsKey(k)) {
                methodParams.put(k, URLDecoder.decode(v));
            }
        });
        return methodParams;
    }

    private Object toObject(Class clazz, String value) {
        if (Boolean.class == clazz || Boolean.TYPE == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz || Byte.TYPE == clazz) return Byte.parseByte(value);
        if (Short.class == clazz || Short.TYPE == clazz) return Short.parseShort(value);
        if (Integer.class == clazz || Integer.TYPE == clazz) return Integer.parseInt(value);
        if (Long.class == clazz || Long.TYPE == clazz) return Long.parseLong(value);
        if (Float.class == clazz || Float.TYPE == clazz) return Float.parseFloat(value);
        if (Double.class == clazz || Double.TYPE == clazz) return Double.parseDouble(value);
        return value;
    }

    private Map<String, String> getParams(String[] split) {
        Map<String, String> paramMap = new HashMap<>();
        if (split.length > 1) {
            String[] queryParams = split[1].split("&");
            for (String queryParam : queryParams) {
                String[] paramKeyValue = queryParam.split("=");
                paramMap.put(paramKeyValue[0], paramKeyValue[1]);
            }
        }
        return paramMap;
    }

    public void loadGuiControllers() {
        Set<Class<?>> typesAnnotatedWith = TubingPlugin.getPlugin().getIocContainer().getReflections().getTypesAnnotatedWith(GuiController.class);

        for (Class<?> aClass : typesAnnotatedWith) {
            List<Method> actionMethods = ReflectionUtils.getMethodsAnnotatedWith(aClass, GuiAction.class);
            for (Method actionMethod : actionMethods) {
                GuiAction annotation = actionMethod.getAnnotation(GuiAction.class);
                String value = annotation.value();
                if (guiActions.containsKey(value)) {
                    throw new IocException("Duplicate GUI action defined: [" + value + "]");
                }
                guiActions.put(value, actionMethod);
            }
        }
    }

    public void removeInventory(Player player) {
        inventories.remove(player.getUniqueId());
    }
}
