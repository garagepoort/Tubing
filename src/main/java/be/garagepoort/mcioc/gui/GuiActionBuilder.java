package be.garagepoort.mcioc.gui;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GuiActionBuilder {

    private final HashMap<String, String> params = new HashMap<>();
    private String action;

    public static GuiActionBuilder builder() {
        return new GuiActionBuilder();
    }

    public GuiActionBuilder action(String action) {
        this.action = action;
        return this;
    }

    public GuiActionBuilder param(String key, String value) {
        if (value != null) {
            String encode = URLEncoder.encode(value);
            params.put(key, encode);
        }
        return this;
    }

    public String build() {
        if (params.isEmpty()) {
            return action;
        }
        String result = action + "?";

        List<String> queryParams = params.keySet().stream()
                .map(key -> key + "=" + params.get(key))
                .collect(Collectors.toList());

        return result + String.join("&", queryParams);
    }
}
