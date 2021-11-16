package be.garagepoort.mcioc.gui.model;

import be.garagepoort.mcioc.IocBean;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

@IocBean
public class InventoryMapper {

    private final ItemStackMapper itemStackMapper;
    private final TextMapper textMapper;

    public InventoryMapper(ItemStackMapper itemStackMapper, TextMapper textMapper) {
        this.itemStackMapper = itemStackMapper;
        this.textMapper = textMapper;
    }

    public Inventory map(TubingGui tubingGui, boolean showIds) {
        Inventory inventory = Bukkit.createInventory(null, tubingGui.getSize(), getId(tubingGui, showIds) + textMapper.mapText(tubingGui.getTitle()).orElse(""));
        tubingGui.getGuiItems().values()
                .stream()
                .filter(guiItem -> !guiItem.isHidden())
                .forEach(guiItem -> inventory.setItem(guiItem.getSlot(), itemStackMapper.map(guiItem, showIds)));
        return inventory;
    }

    private String getId(TubingGui tubingGui, boolean showIds) {
        if (!showIds || !tubingGui.getId().isPresent()) {
            return "";
        }
        return "(" + tubingGui.getId().get().getId().orElse("No ID") + ") ";
    }
}
