package be.garagepoort.mcioc.tubinggui.model;

import org.bukkit.Material;

import java.util.List;

public class TubingGuiItemStack {

    private int amount = 1;
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

    public TubingGuiItemStack(int amount, Material material, TubingGuiText name, boolean enchanted, List<TubingGuiText> loreLines) {
        this.amount = amount;
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

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
