package be.garagepoort.mcioc.gui;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.TubingPlugin;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private final Map<String, Method> guiActions = new HashMap<>();
    private final Map<UUID, TubingGui> inventories = new HashMap<>();

    public void setInventory(Player player, TubingGui tubingGui) {
        inventories.put(player.getUniqueId(), tubingGui);
    }

    public Optional<TubingGui> getTubingGui(Player player) {
        return Optional.ofNullable(inventories.get(player.getUniqueId()));
    }

    public void executeAction(Player player, String actionQuery) {
        try {
            String[] split = actionQuery.split(Pattern.quote("?"), 2);
            String action = split[0];

            if (!guiActions.containsKey(action)) {
                throw new IocException("No Gui Action found for [" + action + "]");
            }

            Method method = guiActions.get(action);

            Object[] methodParams = new Object[method.getParameterCount()];
            methodParams[0] = player;

            Map<String, String> paramMap = getParams(split);
            addMethodParams(method, paramMap, methodParams);

            Object bean = TubingPlugin.getPlugin().getIocContainer().get(method.getDeclaringClass());
            if (bean == null) {
                throw new IocException("No GuiController found to handle action [" + actionQuery + "]. Tried finding [" + method.getClass() + "]");
            }

            Class<?> returnType = method.getReturnType();
            if (returnType == TubingGui.class) {
                TubingGui inventory = (TubingGui) method.invoke(bean, methodParams);
                player.closeInventory();
                player.openInventory(inventory.getInventory());
                setInventory(player, inventory);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IocException("Unable to execute gui action", e);
        }
    }

    private void addMethodParams(Method method, Map<String, String> paramMap, Object[] methodParams) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Optional<Annotation> paramAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(GuiParam.class)).findFirst();
            if (paramAnnotation.isPresent()) {
                GuiParam param = (GuiParam) paramAnnotation.get();
                if (paramMap.containsKey(param.value())) {
                    methodParams[i] = toObject(parameterTypes[i], paramMap.get(param.value()));
                }
            }
        }
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
