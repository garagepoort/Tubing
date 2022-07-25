package be.garagepoort.mcioc.tubinggui;

import java.lang.reflect.Method;

public class GuiActionConfig {

    private final String route;
    private final Method method;
    private final boolean overrideHistory;
    private final boolean skipHistory;

    public GuiActionConfig(String route, Method method, boolean overrideHistory, boolean skipHistory) {
        this.route = route;
        this.method = method;
        this.overrideHistory = overrideHistory;
        this.skipHistory = skipHistory;
    }

    public Method getMethod() {
        return method;
    }

    public String getRoute() {
        return route;
    }

    public boolean isOverrideHistory() {
        return overrideHistory;
    }
    public boolean isSkipHistory() {
        return skipHistory;
    }
}
