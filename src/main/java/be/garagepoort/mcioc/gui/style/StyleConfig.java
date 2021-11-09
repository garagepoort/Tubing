package be.garagepoort.mcioc.gui.style;

import org.bukkit.Material;

import java.util.Optional;

public class StyleConfig {

    private String color;
    private Material material;
    private Boolean hidden;
    private Boolean enchanted;
    private Integer slot;
    private Integer size;

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

    public Optional<Boolean> isHidden() {
        return Optional.of(hidden);
    }

    public Optional<Integer> getSlot() {
        return Optional.ofNullable(slot);
    }

    public StyleConfig update(StyleConfig styleConfig) {
        StyleConfig result = duplicate();
        styleConfig.getSize().ifPresent(s -> result.size = s);
        styleConfig.getSlot().ifPresent(s -> result.slot = s);
        styleConfig.getMaterial().ifPresent(s -> result.material = s);
        styleConfig.getColor().ifPresent(s -> result.color = s);
        styleConfig.isHidden().ifPresent(s -> result.hidden = s);
        styleConfig.isEnchanted().ifPresent(s -> result.enchanted = s);
        return result;
    }

    private StyleConfig duplicate() {
        return new StyleConfig(color, material, hidden, enchanted, slot, size);
    }

    @Override
    public String toString() {
        return "StyleConfig{" +
                "color='" + color + '\'' +
                ", material=" + material +
                ", hidden=" + hidden +
                ", enchanted=" + enchanted +
                ", slot=" + slot +
                ", size=" + size +
                '}';
    }
}
