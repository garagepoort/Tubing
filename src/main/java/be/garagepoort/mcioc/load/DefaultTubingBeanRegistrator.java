package be.garagepoort.mcioc.load;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.IocCommandHandler;
import be.garagepoort.mcioc.IocListener;
import be.garagepoort.mcioc.IocMessageListener;

import java.util.Arrays;
import java.util.List;

public class DefaultTubingBeanRegistrator implements TubingBeanAnnotationRegistrator{

    @Override
    public List<Class> getAnnotations() {
        return Arrays.asList(IocBean.class, IocListener.class, IocCommandHandler.class, IocMessageListener.class);
    }
}
