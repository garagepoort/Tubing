package be.garagepoort.mcioc.gui.model;

import java.util.ArrayList;
import java.util.List;

public class ItemStackLoreLine {

    private List<TubingGuiItemText> parts = new ArrayList<>();

    public void addPart(TubingGuiItemText part) {
        this.parts.add(part);
    }

    public List<TubingGuiItemText> getParts() {
        return parts;
    }


}
