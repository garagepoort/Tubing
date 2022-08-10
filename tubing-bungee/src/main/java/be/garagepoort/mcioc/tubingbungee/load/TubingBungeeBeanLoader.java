package be.garagepoort.mcioc.tubingbungee.load;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.tubingbungee.TubingBungeePlugin;
import be.garagepoort.mcioc.tubingbungee.annotations.IocBungeeCommandHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.List;

public class TubingBungeeBeanLoader {

    public static void load(TubingBungeePlugin tubingPlugin) {
        loadCommandHandlerBeans(tubingPlugin);
    }

    private static void loadCommandHandlerBeans(TubingBungeePlugin tubingPlugin) {
        List<Class<?>> typesAnnotatedWith = tubingPlugin.getIocContainer().getReflections().getClassesWithAnnotation(IocBungeeCommandHandler.class).loadClasses();

        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        for (Class<?> aClass : typesAnnotatedWith) {
            if (!Command.class.isAssignableFrom(aClass)) {
                throw new IocException("IocCommandHandler annotation can only be used on Command classes");
            }
            if (tubingPlugin.getIocContainer().get(aClass) == null) {
                continue;
            }
            Command bean = (Command) tubingPlugin.getIocContainer().get(aClass);
            pluginManager.registerCommand(tubingPlugin, bean);
        }
    }

}
