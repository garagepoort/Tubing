package be.garagepoort.mcioc.gui.style;

import org.bukkit.Material;

import java.util.Optional;

public class StyleConfig {

    private final String color;
    private final Material material;
    private final boolean hidden;
    private final Boolean enchanted;
    private final Integer slot;
    private final Integer size;

    public StyleConfig(String color, Material material, boolean hidden, Boolean enchanted, Integer slot, Integer size) {
        this.color = color;
        this.material = material;
        this.hidden = hidden;
        this.enchanted = enchanted;
        this.slot = slot;
        this.size = size;
    }

    public Optional<String> getColor() {
        return Optional.ofNullable(color);
    }

    public Optional<Material> getMaterial() {
        return Optional.ofNullable(material);
    }

    public Optional<Boolean> isEnchanted() {
        return Optional.ofNullable(enchanted);
    }

    public Optional<Integer> getSize() {
        return Optional.ofNullable(size);
    }

    public boolean isHidden() {
        return hidden;
    }

    public Optional<Integer> getSlot() {
        return Optional.ofNullable(slot);
    }
}
