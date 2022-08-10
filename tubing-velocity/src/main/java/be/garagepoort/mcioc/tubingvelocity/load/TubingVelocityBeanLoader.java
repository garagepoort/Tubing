package be.garagepoort.mcioc.tubingvelocity.load;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.configuration.ConfigurationLoader;
import be.garagepoort.mcioc.tubingvelocity.TubingVelocityPlugin;
import be.garagepoort.mcioc.tubingvelocity.annotations.IocVelocityCommandHandler;
import be.garagepoort.mcioc.tubingvelocity.annotations.IocVelocityListener;
import be.garagepoort.mcioc.tubingvelocity.exceptions.TubingVelocityException;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.Arrays;
import java.util.List;

public class TubingVelocityBeanLoader {

    public static void load(TubingVelocityPlugin tubingPlugin) {
        loadCommandHandlerBeans(tubingPlugin);
        loadListenerBeans(tubingPlugin);
    }

    private static void loadCommandHandlerBeans(TubingVelocityPlugin tubingPlugin) {
        List<Class<?>> typesAnnotatedWith = tubingPlugin.getIocContainer().getReflections().getClassesWithAnnotation(IocVelocityCommandHandler.class).loadClasses();

        for (Class<?> aClass : typesAnnotatedWith) {
            if (!SimpleCommand.class.isAssignableFrom(aClass)) {
                throw new IocException("IocVelocityCommandHandler annotation can only be used on SimpleCommand classes");
            }
            if (tubingPlugin.getIocContainer().get(aClass) == null) {
                continue;
            }
            SimpleCommand bean = (SimpleCommand) tubingPlugin.getIocContainer().get(aClass);
            ConfigurationLoader configurationLoader = tubingPlugin.getIocContainer().get(ConfigurationLoader.class);

            IocVelocityCommandHandler annotation = aClass.getAnnotation(IocVelocityCommandHandler.class);
            String command = mapPropertyReference(configurationLoader, annotation.value());
            String[] aliases = Arrays.stream(annotation.aliases())
                .map(alias -> mapPropertyReference(configurationLoader, alias))
                .toArray(String[]::new);

            tubingPlugin.getCommandManager().register(command, bean, aliases);
        }
    }

    private static String mapPropertyReference(ConfigurationLoader configurationLoader, String value) {
        if (value.contains(":")) {
            return configurationLoader.getConfigStringValue(value).orElseThrow(() -> new TubingVelocityException("Invalid property reference: " + value));
        }
        return value;
    }

    private static void loadListenerBeans(TubingVelocityPlugin tubingPlugin) {
        List<Class<?>> typesAnnotatedWith = tubingPlugin.getIocContainer().getReflections().getClassesWithAnnotation(IocVelocityListener.class).loadClasses();

        for (Class<?> aClass : typesAnnotatedWith) {
            if (tubingPlugin.getIocContainer().get(aClass) == null) {
                continue;
            }
            Object bean = tubingPlugin.getIocContainer().get(aClass);
            tubingPlugin.getEventManager().register(tubingPlugin, bean);
        }
    }
}
