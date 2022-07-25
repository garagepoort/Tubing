package be.garagepoort.mcioc.tubinggui.templates;

import java.util.Map;

public class GuiTemplate {
    private final String template;
    private final Map<String, Object> params;

    private GuiTemplate(String template, Map<String, Object> params) {
        this.template = template;
        this.params = params;
    }

    public static GuiTemplate template(String template, Map<String, Object> params) {
        return new GuiTemplate(template, params);
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
