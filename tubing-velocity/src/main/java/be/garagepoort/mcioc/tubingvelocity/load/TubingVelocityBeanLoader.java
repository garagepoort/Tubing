package be.garagepoort.mcioc.tubingvelocity.load;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.configuration.ConfigurationLoader;
import be.garagepoort.mcioc.diagnostics.TubingDiagnosticException;
import be.garagepoort.mcioc.tubingvelocity.TubingVelocityPlugin;
import be.garagepoort.mcioc.tubingvelocity.annotations.IocVelocityCommandHandler;
import be.garagepoort.mcioc.tubingvelocity.annotations.IocVelocityListener;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.Collections;
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
            String command = mapPropertyReference(configurationLoader, annotation.value(), aClass, "value");
            String[] aliases = Arrays.stream(annotation.aliases())
                .map(alias -> mapPropertyReference(configurationLoader, alias, aClass, "aliases"))
                .toArray(String[]::new);

            tubingPlugin.getCommandManager().register(command, bean, aliases);
        }
    }

    private static String mapPropertyReference(ConfigurationLoader configurationLoader, String value, Class<?> commandClass, String annotationField) {
        if (value.contains(":")) {
            return configurationLoader.getConfigStringValue(value).orElseThrow(() -> new TubingDiagnosticException(
                "Invalid Velocity command configuration",
                Arrays.asList(
                    "Class: " + commandClass.getName(),
                    "Annotation: @IocVelocityCommandHandler(" + annotationField + " = " + value + ")",
                    "Property: " + value
                ),
                Collections.singletonList("Add the property to the yaml file, or use a literal command name instead of a config reference.")
            ));
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
