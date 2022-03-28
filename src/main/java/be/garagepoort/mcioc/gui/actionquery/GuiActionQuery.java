package be.garagepoort.mcioc.gui.actionquery;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GuiActionQuery {

    private final String fullQuery;
    private final String route;
    private final Map<String, String> params;

    public GuiActionQuery(String actionQuery) {
        this.fullQuery = actionQuery;
        this.params = getParams(actionQuery);
        this.route = actionQuery.split(Pattern.quote("?"), 2)[0];
    }

    public String getFullQuery() {
        return fullQuery;
    }

    public String getRoute() {
        return route;
    }

    public Map<String, String> getParams() {
        return params;
    }

    private Map<String, String> getParams(String actionQuery) {
        String[] split = actionQuery.split(Pattern.quote("?"), 2);
        Map<String, String> paramMap = new HashMap<>();
        if (split.length > 1) {
            String[] queryParams = split[1].split("&");
            for (String queryParam : queryParams) {
                String[] paramKeyValue = queryParam.split("=");
                paramMap.put(paramKeyValue[0], URLDecoder.decode(paramKeyValue[1]));
            }
        }
        return paramMap;
    }

}
