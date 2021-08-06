package be.garagepoort.mcioc.gui.templates;

import java.util.Map;

public class ChatTemplate {
    private final String template;
    private final Map<String, Object> params;

    private ChatTemplate(String template, Map<String, Object> params) {
        this.template = template;
        this.params = params;
    }

    public static ChatTemplate chatTemplate(String template, Map<String, Object> params) {
        return new ChatTemplate(template, params);
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
