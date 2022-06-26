package be.garagepoort.mcioc.tubinggui.load;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.IocContainer;
import be.garagepoort.mcioc.IocMultiProvider;
import be.garagepoort.mcioc.load.OnLoad;
import be.garagepoort.mcioc.tubinggui.GuiActionService;

@IocBean
@IocMultiProvider(OnLoad.class)
public class TubingGuiOnload implements OnLoad {

    private final GuiActionService guiActionService;

    public TubingGuiOnload(GuiActionService guiActionService) {
        this.guiActionService = guiActionService;
    }

    public void load(IocContainer iocContainer) {
        guiActionService.loadGuiControllers(iocContainer);
    }
}
