package be.garagepoort.mcioc.tubingvelocity.load;

import be.garagepoort.mcioc.load.TubingBeanAnnotationRegistrator;
import be.garagepoort.mcioc.tubingvelocity.annotations.IocVelocityCommandHandler;
import be.garagepoort.mcioc.tubingvelocity.annotations.IocVelocityListener;

import java.util.Arrays;
import java.util.List;

public class TubingVelocityBeanRegistrator implements TubingBeanAnnotationRegistrator {

    @Override
    public List<Class> getAnnotations() {
        return Arrays.asList(IocVelocityListener.class, IocVelocityCommandHandler.class);
    }
}
