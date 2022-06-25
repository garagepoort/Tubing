package be.garagepoort.mcioc.load;

import be.garagepoort.mcioc.IocBean;

import java.util.Arrays;
import java.util.List;

public class DefaultTubingBeanRegistrator implements TubingBeanAnnotationRegistrator{

    @Override
    public List<Class> getAnnotations() {
        return Arrays.asList(IocBean.class);
    }
}
