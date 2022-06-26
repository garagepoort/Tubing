package be.garagepoort.mcioc.tubinggui.model;

import java.util.ArrayList;
import java.util.List;

public class TubingGuiText {

    private boolean hidden;
    private String color;
    private final List<TubingGuiTextPart> parts = new ArrayList<>();

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void addPart(TubingGuiTextPart part) {
        this.parts.add(part);
    }

    public List<TubingGuiTextPart> getParts() {
        return parts;
    }

}
