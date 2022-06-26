package be.garagepoort.mcioc.tubingbungee.load;

import be.garagepoort.mcioc.load.TubingBeanAnnotationRegistrator;
import be.garagepoort.mcioc.tubingbungee.annotations.IocBungeeCommandHandler;
import be.garagepoort.mcioc.tubingbungee.annotations.IocBungeeListener;

import java.util.Arrays;
import java.util.List;

public class TubingBungeeBeanRegistrator implements TubingBeanAnnotationRegistrator {

    @Override
    public List<Class> getAnnotations() {
        return Arrays.asList(IocBungeeListener.class, IocBungeeCommandHandler.class);
    }
}
