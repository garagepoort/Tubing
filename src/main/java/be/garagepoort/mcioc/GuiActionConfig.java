package be.garagepoort.mcioc;

import java.lang.reflect.Method;

public class GuiActionConfig {

    private final String route;
    private final Method method;
    private final boolean overrideHistory;

    public GuiActionConfig(String route, Method method, boolean overrideHistory) {
        this.route = route;
        this.method = method;
        this.overrideHistory = overrideHistory;
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
}
