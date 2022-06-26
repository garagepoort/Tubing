package be.garagepoort.mcioc.tubinggui;

import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GuiActionBuilder {

    private Map<String, String> params = new HashMap<>();
    private String action;

    public static GuiActionBuilder builder() {
        return new GuiActionBuilder();
    }

    public static GuiActionBuilder fromAction(String actionQuery) {

        String[] split = actionQuery.split(Pattern.quote("?"), 2);
        GuiActionBuilder actionBuilder = new GuiActionBuilder();
        actionBuilder.action = split[0];
        actionBuilder.params = getParams(actionQuery);

        return actionBuilder;
    }

    private static Map<String, String> getParams(String actionQuery) {
        String[] split = actionQuery.split(Pattern.quote("?"), 2);
        Map<String, String> paramMap = new HashMap<>();
        if (split.length > 1) {
            String[] queryParams = split[1].split("&");
            for (String queryParam : queryParams) {
                String[] paramKeyValue = queryParam.split("=");
                paramMap.put(paramKeyValue[0], paramKeyValue[1]);
            }
        }
        return paramMap;
    }

    public GuiActionBuilder action(String action) {
        this.action = action;
        return this;
    }

    public GuiActionBuilder param(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
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
