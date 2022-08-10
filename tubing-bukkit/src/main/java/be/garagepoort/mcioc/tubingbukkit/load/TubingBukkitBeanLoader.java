package be.garagepoort.mcioc.tubingbukkit.load;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.tubingbukkit.TubingBukkitPlugin;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitCommandHandler;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitListener;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitMessageListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.List;

public class TubingBukkitBeanLoader {

    public static void load(TubingPlugin tubingPlugin) {
        loadCommandHandlerBeans(tubingPlugin);
        loadListenerBeans(tubingPlugin);
        loadMessageListenerBeans(tubingPlugin);
    }

    private static void loadCommandHandlerBeans(TubingPlugin tubingPlugin) {
        List<Class<?>> typesAnnotatedWith = tubingPlugin.getIocContainer().getReflections().getClassesWithAnnotation(IocBukkitCommandHandler.class).loadClasses();

        for (Class<?> aClass : typesAnnotatedWith) {
            if (!CommandExecutor.class.isAssignableFrom(aClass)) {
                throw new IocException("IocCommandHandler annotation can only be used on CommandExecutors");
            }
            if (tubingPlugin.getIocContainer().get(aClass) == null) {
                continue;
            }
            CommandExecutor bean = (CommandExecutor) tubingPlugin.getIocContainer().get(aClass);
            IocBukkitCommandHandler annotation = aClass.getAnnotation(IocBukkitCommandHandler.class);
            TubingBukkitPlugin.getPlugin().getCommand(annotation.value()).setExecutor(bean);
        }
    }

    private static void loadListenerBeans(TubingPlugin tubingPlugin) {
        List<Class<?>> typesAnnotatedWith = tubingPlugin.getIocContainer().getReflections().getClassesWithAnnotation(IocBukkitListener.class).loadClasses();

        for (Class<?> aClass : typesAnnotatedWith) {
            if (!Listener.class.isAssignableFrom(aClass)) {
                throw new IocException("IocListener annotation can only be used on bukkit Listeners. Failing class [" + aClass + "]");
            }
            if (tubingPlugin.getIocContainer().get(aClass) == null) {
                continue;
            }
            Listener bean = (Listener) tubingPlugin.getIocContainer().get(aClass);
            Bukkit.getPluginManager().registerEvents(bean, TubingBukkitPlugin.getPlugin());
        }
    }

    private static void loadMessageListenerBeans(TubingPlugin tubingPlugin) {
        List<Class<?>> typesAnnotatedWith = tubingPlugin.getIocContainer().getReflections().getClassesWithAnnotation(IocBukkitMessageListener.class).loadClasses();

        for (Class<?> aClass : typesAnnotatedWith) {
            if (!PluginMessageListener.class.isAssignableFrom(aClass)) {
                throw new IocException("IocMessageListener annotation can only be used on bukkit PluginMessageListeners");
            }
            if (tubingPlugin.getIocContainer().get(aClass) == null) {
                continue;
            }
            PluginMessageListener bean = (PluginMessageListener) tubingPlugin.getIocContainer().get(aClass);
            IocBukkitMessageListener annotation = aClass.getAnnotation(IocBukkitMessageListener.class);
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(TubingBukkitPlugin.getPlugin(), annotation.channel(), bean);
        }
    }
}
