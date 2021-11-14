package be.garagepoort.mcioc.gui.model;

import be.garagepoort.mcioc.IocBean;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

@IocBean
public class InventoryMapper {

    private final ItemStackMapper itemStackMapper;

    public InventoryMapper(ItemStackMapper itemStackMapper) {
        this.itemStackMapper = itemStackMapper;
    }

    public Inventory map(TubingGui tubingGui, boolean showIds) {
        Inventory inventory = Bukkit.createInventory(null, tubingGui.getSize(), getId(tubingGui, showIds) + tubingGui.getTitle());
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
        return "(" + tubingGui.getId().get().getFullId().split("_")[0] + ") ";
    }
}
