package be.garagepoort.mcioc.tubinggui;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.IocContainer;
import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.IocMulti;
import be.garagepoort.mcioc.ReflectionUtils;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.configuration.ConfigurationLoader;
import be.garagepoort.mcioc.load.InjectTubingPlugin;
import be.garagepoort.mcioc.tubingbukkit.common.ITubingBukkitUtil;
import be.garagepoort.mcioc.tubinggui.actionquery.ActionQueryParser;
import be.garagepoort.mcioc.tubinggui.actionquery.GuiActionQuery;
import be.garagepoort.mcioc.tubinggui.exceptions.GuiExceptionHandler;
import be.garagepoort.mcioc.tubinggui.exceptions.GuiExceptionHandlerProvider;
import be.garagepoort.mcioc.tubinggui.history.GuiHistoryStack;
import be.garagepoort.mcioc.tubinggui.model.InventoryMapper;
import be.garagepoort.mcioc.tubinggui.model.TubingChatGui;
import be.garagepoort.mcioc.tubinggui.model.TubingGui;
import be.garagepoort.mcioc.tubinggui.model.TubingGuiActions;
import be.garagepoort.mcioc.tubinggui.style.TubingGuiStyleIdViewProvider;
import be.garagepoort.mcioc.tubinggui.templates.ChatTemplate;
import be.garagepoort.mcioc.tubinggui.templates.ChatTemplateResolver;
import be.garagepoort.mcioc.tubinggui.templates.GuiTemplate;
import be.garagepoort.mcioc.tubinggui.templates.GuiTemplateProcessor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.stream;

@IocBean
public class GuiActionService {
    private final Map<String, GuiActionConfig> guiActions = new HashMap<>();
    private final Map<UUID, TubingGui> inventories = new HashMap<>();
    public final Map<UUID, Boolean> isOpeningInventory = new HashMap<>();
    private final Map<Class<? extends Exception>, GuiExceptionHandler> exceptionHandlers = new HashMap<>();

    private final TubingPlugin tubingPlugin;
    private final ConfigurationLoader configurationLoader;
    private final GuiTemplateProcessor guiTemplateProcessor;
    private final ChatTemplateResolver chatTemplateResolver;
    private final ActionQueryParser actionQueryParser;
    private final ITubingBukkitUtil tubingBukkitUtil;
    private final InventoryMapper inventoryMapper;
    private final TubingGuiStyleIdViewProvider tubingGuiStyleIdViewProvider;
    private final GuiHistoryStack guiHistoryStack;

    public GuiActionService(
                            @InjectTubingPlugin TubingPlugin tubingPlugin,
                            ConfigurationLoader configurationLoader,
                            GuiTemplateProcessor guiTemplateProcessor,
                            ChatTemplateResolver chatTemplateResolver,
                            ActionQueryParser actionQueryParser,
                            ITubingBukkitUtil tubingBukkitUtil,
                            InventoryMapper inventoryMapper,
                            TubingGuiStyleIdViewProvider tubingGuiStyleIdViewProvider, GuiHistoryStack guiHistoryStack,
                            @IocMulti(GuiExceptionHandler.class) List<GuiExceptionHandler> providedExceptionHandlers) {
        this.tubingPlugin = tubingPlugin;
        this.configurationLoader = configurationLoader;
        this.guiTemplateProcessor = guiTemplateProcessor;
        this.chatTemplateResolver = chatTemplateResolver;
        this.actionQueryParser = actionQueryParser;
        this.tubingBukkitUtil = tubingBukkitUtil;
        this.inventoryMapper = inventoryMapper;
        this.tubingGuiStyleIdViewProvider = tubingGuiStyleIdViewProvider;
        this.guiHistoryStack = guiHistoryStack;
        registerExceptionHandlers(providedExceptionHandlers);
    }

    private void registerExceptionHandlers(List<GuiExceptionHandler> providedExceptionHandlers) {
        for (GuiExceptionHandler providedExceptionHandler : providedExceptionHandlers) {
            stream(providedExceptionHandler.getClass().getAnnotations())
                .filter(a -> a.annotationType().equals(GuiExceptionHandlerProvider.class))
                .map(a -> (GuiExceptionHandlerProvider) a)
                .findFirst()
                .ifPresent(a -> stream(a.exceptions()).forEach(aClass -> registerExceptionHandler(aClass, providedExceptionHandler)));
        }
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
            if (actionQuery.equalsIgnoreCase(TubingGuiActions.BACK)) {
                Optional<String> backAction = guiHistoryStack.pop(player.getUniqueId());
                if (backAction.isPresent()) {
                    executeAction(player, backAction.get());
                    return;
                }
                player.closeInventory();
                removeInventory(player);
                return;
            }

            GuiActionQuery guiActionQuery = new GuiActionQuery(actionQuery);

            if (!guiActions.containsKey(guiActionQuery.getRoute())) {
                throw new IocException("No Gui Action found for [" + guiActionQuery.getRoute() + "]");
            }

            GuiActionConfig guiActionConfig = guiActions.get(guiActionQuery.getRoute());
            Method method = guiActionConfig.getMethod();
            Object[] methodParams = actionQueryParser.getMethodParams(method, guiActionQuery, player, getTubingGui(player).orElse(null));

            Object bean = tubingPlugin.getIocContainer().get(method.getDeclaringClass());
            if (bean == null) {
                throw new IocException("No GuiController found to handle action [" + actionQuery + "]. Tried finding [" + method.getClass() + "]");
            }

            try {
                Object invokedReturnedObject = method.invoke(bean, methodParams);
                if (invokedReturnedObject instanceof AsyncGui) {
                    processAsyncGuiAction(player, guiActionQuery, guiActionConfig, method, (AsyncGui) invokedReturnedObject);
                } else {
                    processGuiAction(player, guiActionQuery, guiActionConfig, method, invokedReturnedObject);
                }
            } catch (InvocationTargetException e) {
                handleException(player, e.getCause());
            }
        } catch (Throwable e) {
            throw new IocException("Unable to execute gui action", e);
        }
    }

    private void processAsyncGuiAction(Player player, GuiActionQuery actionQuery, GuiActionConfig guiActionConfig, Method method, AsyncGui invokedReturnedObject) {
        tubingBukkitUtil.runAsync(() -> {
            try {
                AsyncGui asyncGui = invokedReturnedObject;
                Object run = asyncGui.getAsyncGuiExecutor().run();
                tubingBukkitUtil.runTaskLater(() -> processGuiAction(player, actionQuery, guiActionConfig, method, run), 1);
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
            guiHistoryStack.clear(player.getUniqueId());
            return;
        }

        Optional<Class<? extends Exception>> parentException = exceptionHandlers.keySet().stream()
            .filter(c -> c.isAssignableFrom(e.getClass()))
            .findFirst();

        if (parentException.isPresent()) {
            exceptionHandlers.get(parentException.get()).accept(player, e);
            player.closeInventory();
            removeInventory(player);
            guiHistoryStack.clear(player.getUniqueId());
            return;
        }

        throw e;
    }

    private void processGuiAction(Player player,
                                  GuiActionQuery actionQuery,
                                  GuiActionConfig guiActionConfig,
                                  Method method,
                                  Object invokedReturnedObject) {
        guiHistoryStack.push(player.getUniqueId(), actionQuery, guiActionConfig.isOverrideHistory(), guiActionConfig.isSkipHistory());
        if (invokedReturnedObject instanceof TubingGui) {
            TubingGui tubingGui = (TubingGui) invokedReturnedObject;
            showGui(player, tubingGui);
        } else if (invokedReturnedObject == null) {
            player.closeInventory();
            removeInventory(player);
            guiHistoryStack.clear(player.getUniqueId());
        } else if (invokedReturnedObject instanceof GuiActionReturnType) {
            GuiActionReturnType actionReturnType = (GuiActionReturnType) invokedReturnedObject;
            if (actionReturnType == GuiActionReturnType.BACK) {
                executeAction(player, TubingGuiActions.BACK);
            } else if (actionReturnType != GuiActionReturnType.KEEP_OPEN) {
                player.closeInventory();
                removeInventory(player);
            }
        } else if (invokedReturnedObject instanceof GuiTemplate) {
            GuiTemplate guiTemplate = (GuiTemplate) invokedReturnedObject;
            addTemplateParams(player, actionQuery, method, guiTemplate.getParams());
            showGuiTemplate(player, guiTemplate, actionQuery, guiActionConfig);
        } else if (invokedReturnedObject instanceof ChatTemplate) {
            ChatTemplate chatTemplate = (ChatTemplate) invokedReturnedObject;
            addTemplateParams(player, actionQuery, method, chatTemplate.getParams());
            showChatTemplate(player, chatTemplate);
            guiHistoryStack.clear(player.getUniqueId());
        } else if (invokedReturnedObject instanceof String) {
            String redirectAction = (String) invokedReturnedObject;
            executeAction(player, redirectAction);
        } else {
            throw new IocException("Invalid returnType [" + invokedReturnedObject.getClass() + "] for GuiController [" + method.getDeclaringClass() + "]");
        }
    }

    public void showGuiTemplate(Player player, GuiTemplate guiTemplate, GuiActionQuery actionQuery, GuiActionConfig guiActionConfig) {
        showGui(player, guiTemplateProcessor.process(player,
            guiTemplate.getTemplate(),
            guiTemplate.getParams())
        );
    }

    public void showChatTemplate(Player player, ChatTemplate chatTemplate) {
        showChat(player, chatTemplateResolver.resolve(chatTemplate.getTemplate(), chatTemplate.getParams()));
    }

    public void showGui(Player player, TubingGui tubingGui) {
        tubingBukkitUtil.runTaskLater(() -> {
            isOpeningInventory.put(player.getUniqueId(), true);
            player.closeInventory();
            boolean showId = tubingGuiStyleIdViewProvider.canView(player);
            Inventory inventory = inventoryMapper.map(tubingGui, showId);
            tubingGui.setInventory(inventory);
            player.openInventory(inventory);
            setInventory(player, tubingGui);
            isOpeningInventory.put(player.getUniqueId(), false);
        }, 1);
    }

    public void showChat(Player player, TubingChatGui tubingChatGui) {
        tubingBukkitUtil.runTaskLater(() -> {
            for (String chatLine : tubingChatGui.getChatLines()) {
                player.sendMessage(chatLine);
            }
        }, 1);
    }

    private Map<String, Object> getTemplateParams(Method method, GuiActionQuery actionQuery, Player player) {
        Map<String, Object> methodParams = new HashMap<>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Optional<Annotation> paramAnnotation = stream(annotations).filter(a -> a.annotationType().equals(GuiParam.class)).findFirst();
            if (paramAnnotation.isPresent()) {
                GuiParam param = (GuiParam) paramAnnotation.get();
                if (actionQuery.getParams().containsKey(param.value())) {
                    methodParams.put(param.value(), toObject(parameterTypes[i], actionQuery.getParams().get(param.value())));
                } else if (StringUtils.isNotBlank(param.defaultValue())) {
                    methodParams.put(param.value(), toObject(parameterTypes[i], param.defaultValue()));
                }
            }
        }
        methodParams.put("player", player);
        methodParams.put("currentAction", actionQuery.getFullQuery());
        actionQuery.getParams().forEach((k, v) -> {
            if (!methodParams.containsKey(k)) {
                methodParams.put(k, v);
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

    public void loadGuiControllers(IocContainer iocContainer) {
        List<Class<?>> typesAnnotatedWith = iocContainer.getReflections().getClassesWithAnnotation(GuiController.class).loadClasses();
        typesAnnotatedWith.forEach(this::loadGuiController);
    }

    public void loadGuiController(Class guiController) {
        List<Method> actionMethods = ReflectionUtils.getMethodsAnnotatedWith(guiController, GuiAction.class);
        for (Method actionMethod : actionMethods) {
            GuiAction annotation = actionMethod.getAnnotation(GuiAction.class);
            String value = annotation.value();
            boolean overrideHistory = annotation.overrideHistory();
            boolean skipHistory = annotation.skipHistory();
            if (guiActions.containsKey(value)) {
                throw new IocException("Duplicate GUI action defined: [" + value + "]");
            }
            guiActions.put(value, new GuiActionConfig(value, actionMethod, overrideHistory, skipHistory));
        }
    }

    public void removeInventory(Player player) {
        inventories.remove(player.getUniqueId());
    }

    private void addTemplateParams(Player player, GuiActionQuery actionQuery, Method method, Map<String, Object> params) {
        Map<String, Object> templateParams = getTemplateParams(method, actionQuery, player);
        templateParams.forEach((k, v) -> {
            if (!params.containsKey(k)) {
                params.put(k, v);
            }
        });
    }
}
