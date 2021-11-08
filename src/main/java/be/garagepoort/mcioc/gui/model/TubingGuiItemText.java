package be.garagepoort.mcioc.gui.model;

public class TubingGuiItemText {
    private String id;
    private String text;
    private String color;
    private boolean hidden;

    public TubingGuiItemText(String text, String color) {
        this.text = text;
        this.color = color;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color == null ? text : color + text;
    }
}
