package be.garagepoort.mcioc.gui.templates.xml.style;

import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class StyleId {

    private final String path;
    private String id;
    private List<String> classes;

    public StyleId(String path, String id, List<String> classes) {
        this.path = path;
        this.id = id;
        this.classes = classes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getFullId() {
        if (id == null) {
            return null;
        }
        return StringUtils.isBlank(path) ? id : path + "_" + id;
    }

    public boolean matchesClassSelector(String selector) {
        if (!selector.contains("$")) {
            throw new TubingGuiException("No class selector: [" + selector + "]");
        }
        if (selector.startsWith("$")) {
            return classes.contains(selector.replace("$", ""));
        }

        String[] split = selector.split("_\\$");
        if (split.length != 2) {
            throw new TubingGuiException("Invalid class selector: [" + selector + "]");
        }
        String selectWithoutClass = split[0];
        String className = split[1];

        return path.contains(selectWithoutClass) && classes.contains(className);
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }
}