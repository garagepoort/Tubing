package be.garagepoort.mcioc.gui.templates.xml.style;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StyleId {

    private final StyleId parent;
    private String id;
    private List<String> classes;

    public StyleId(StyleId parent, String id, List<String> classes) {
        this.parent = parent;
        this.id = id;
        this.classes = classes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public boolean matchesIdSelector(String selector) {
        int index = selector.lastIndexOf("_");
        String last = selector;
        if(index > -1) {
            if(parent == null) {
                //The selector has a parent but we do not.
                return false;
            }

            String parentSelector = selector.substring(0, index);
            last = selector.substring(index + 1);
            if(last.startsWith("$")) {
                boolean classMatched = classes.contains(last.replace("$", ""));
                return classMatched && parent.matchesIdSelector(parentSelector);
            }
            return this.id != null && this.id.equals(last) && parent.matchesIdSelector(parentSelector);
        }else {
            if(parent != null) {
                //we reached the end of the selector but our parent is not included
                return false;
            }

            if(last.startsWith("$")) {
                return classes.contains(last.replace("$", ""));
            }
            return this.id != null && this.id.equals(last);
        }
    }

    public boolean matchesClass(String selector) {
        int index = selector.lastIndexOf("_");
        String last = selector;
        if(index > -1) {
            String parentSelector = selector.substring(0, index);
            last = selector.substring(index + 1);
            if(!last.startsWith("$") || !classes.contains(last.replace("$", ""))) {
                return false;
            }
            if(parent == null) {
                //The selector has a parent but we do not.
                return false;
            }
            return parent.matchesClassSelector(parentSelector);
        }
        return last.startsWith("$") && classes.contains(last.replace("$", ""));
    }

    private boolean matchesClassSelector(String selector) {
        int index = selector.lastIndexOf("_");
        String last = selector;
        if(index > -1) {
            if(parent == null) {
                //The selector has a parent but we do not.
                return false;
            }

            String parentSelector = selector.substring(0, index);
            last = selector.substring(index + 1);
            if(last.startsWith("$")) {
                boolean classMatched = classes.contains(last.replace("$", ""));
                return (classMatched && parent.matchesClassSelector(parentSelector)) || parent.matchesClassSelector(selector);
            }
            return (this.id != null && this.id.equals(last) && parent.matchesClassSelector(parentSelector)) || parent.matchesClassSelector(selector);
        }else {
            if(last.startsWith("$")) {
                return classes.contains(last.replace("$", ""));
            }
            return (this.id != null && this.id.equals(last)) || (this.parent != null && parent.matchesClassSelector(last));
        }
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        return "StyleId{" +
                "path='" + parent + '\'' +
                ", id='" + id + '\'' +
                ", classes=" + classes.stream().collect(Collectors.joining(",")) +
                '}';
    }
}
