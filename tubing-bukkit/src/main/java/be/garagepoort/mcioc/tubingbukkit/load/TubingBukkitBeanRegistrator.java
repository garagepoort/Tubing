package be.garagepoort.mcioc.tubingbukkit.load;

import be.garagepoort.mcioc.load.TubingBeanAnnotationRegistrator;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitCommandHandler;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitListener;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitMessageListener;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitSubCommand;

import java.util.Arrays;
import java.util.List;

public class TubingBukkitBeanRegistrator implements TubingBeanAnnotationRegistrator {

    @Override
    public List<Class> getAnnotations() {
        return Arrays.asList(IocBukkitListener.class, IocBukkitMessageListener.class, IocBukkitCommandHandler.class, IocBukkitSubCommand.class);
    }
}
