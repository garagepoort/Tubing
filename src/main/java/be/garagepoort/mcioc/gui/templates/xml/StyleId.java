package be.garagepoort.mcioc.gui.templates.xml;

import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;

import java.util.List;
import java.util.Optional;

public class StyleId {

    private final String id;
    private List<String> classes;
    private final StyleId parent;

    public StyleId(String id, StyleId parent, List<String> classes) {
        this.id = id;
        this.parent = parent;
        this.classes = classes;
    }

    public String getId() {
        return id;
    }

    public String getFullId() {
        if(id == null) {
            return null;
        }
        return parent == null ? id : getFullId() + "_" + id;
    }

    public Optional<StyleId> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean hasParent() {
        return parent != null;
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

        return getFullId().contains(selectWithoutClass) && classes.contains(className);
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }
}
