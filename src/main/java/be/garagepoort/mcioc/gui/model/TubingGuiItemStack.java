package be.garagepoort.mcioc.gui.model;

import org.bukkit.Material;

import java.util.List;

public class TubingGuiItemStack {

    private Material material;
    private final TubingGuiText name;
    private boolean enchanted;
    private final List<TubingGuiText> loreLines;

    public TubingGuiItemStack(Material material, TubingGuiText name, boolean enchanted, List<TubingGuiText> loreLines) {
        this.material = material;
        this.name = name;
        this.enchanted = enchanted;
        this.loreLines = loreLines;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public List<TubingGuiText> getLoreLines() {
        return loreLines;
    }

    public TubingGuiText getName() {
        return name;
    }

    public boolean isEnchanted() {
        return enchanted;
    }

    public void setEnchanted(boolean enchanted) {
        this.enchanted = enchanted;
    }

}
