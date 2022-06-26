package be.garagepoort.mcioc.tubinggui.load;

import be.garagepoort.mcioc.load.TubingBeanAnnotationRegistrator;
import be.garagepoort.mcioc.tubinggui.GuiController;
import be.garagepoort.mcioc.tubinggui.exceptions.GuiExceptionHandlerProvider;

import java.util.Arrays;
import java.util.List;

public class TubingGuiBeanRegistrator implements TubingBeanAnnotationRegistrator {

    @Override
    public List<Class> getAnnotations() {
        return Arrays.asList(GuiController.class, GuiExceptionHandlerProvider.class);
    }
}
