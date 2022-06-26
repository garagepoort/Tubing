package be.garagepoort.mcioc.tubingbungee.load;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.tubingbungee.TubingBungeePlugin;
import be.garagepoort.mcioc.tubingbungee.annotations.IocBungeeCommandHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.Set;

public class TubingBungeeBeanLoader {

    public static void load(TubingBungeePlugin tubingPlugin) {
        loadCommandHandlerBeans(tubingPlugin);
//        loadListenerBeans(TubingBungeePlugin);
    }

    private static void loadCommandHandlerBeans(TubingBungeePlugin tubingPlugin) {
        Set<Class<?>> typesAnnotatedWith = tubingPlugin.getIocContainer().getReflections().getTypesAnnotatedWith(IocBungeeCommandHandler.class);

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

//    private static void loadListenerBeans(TubingPlugin tubingPlugin) {
//        Set<Class<?>> typesAnnotatedWith = tubingPlugin.getIocContainer().getReflections().getTypesAnnotatedWith(IocBungeeListener.class);
//
//        for (Class<?> aClass : typesAnnotatedWith) {
//            if (!Listener.class.isAssignableFrom(aClass)) {
//                throw new IocException("IocListener annotation can only be used on bukkit Listeners. Failing class [" + aClass + "]");
//            }
//            if (tubingPlugin.getIocContainer().get(aClass) == null) {
//                continue;
//            }
//            Listener bean = (Listener) tubingPlugin.getIocContainer().get(aClass);
//            Bukkit.getPluginManager().registerEvents(bean, TubingBukkitPlugin.getPlugin());
//        }
//    }
}
