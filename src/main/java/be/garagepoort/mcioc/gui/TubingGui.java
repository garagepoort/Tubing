package be.garagepoort.mcioc.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TubingGui {

    private final String id;
    private final Inventory inventory;
    private final Map<Integer, TubingGuiItem> guiItems = new HashMap<>();

    public TubingGui(Inventory inventory, List<TubingGuiItem> guiItems, String id) {
        this.inventory = inventory;
        this.id = id;
        for (TubingGuiItem guiItem : guiItems) {
            addItem(guiItem);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void addItem(TubingGuiItem tubingGuiItem) {
        this.guiItems.put(tubingGuiItem.getSlot(), tubingGuiItem);
        inventory.setItem(tubingGuiItem.getSlot(), tubingGuiItem.getItemStack());
    }

    public String getId() {
        return id;
    }

    public Map<Integer, String> getLeftActions() {
        Map<Integer, String> actions = new HashMap<>();
        for (TubingGuiItem guiItem : guiItems.values()) {
            actions.put(guiItem.getSlot(), guiItem.getLeftClickAction());
        }
        return actions;
    }

    public Map<Integer, String> getRightActions() {
        Map<Integer, String> actions = new HashMap<>();
        for (TubingGuiItem guiItem : guiItems.values()) {
            actions.put(guiItem.getSlot(), guiItem.getRightClickAction());
        }
        return actions;
    }

    public Map<Integer, String> getMiddleActions() {
        Map<Integer, String> actions = new HashMap<>();
        for (TubingGuiItem guiItem : guiItems.values()) {
            actions.put(guiItem.getSlot(), guiItem.getMiddleClickAction());
        }
        return actions;
    }

    public static class Builder {
        protected final Inventory inventory;
        private String id;
        private List<TubingGuiItem> guiItems = new ArrayList<>();

        public Builder(String title, int size) {
            this.inventory = Bukkit.createInventory(null, size, title);
        }

        public Builder(String title, int size, String guiId) {
            this.inventory = Bukkit.createInventory(null, size, title);
            this.id = guiId;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder addItem(TubingGuiItem tubingGuiItem) {
            this.guiItems.add(tubingGuiItem);
            return this;
        }

        @Deprecated
        public Builder addItem(String leftClickAction, String rightClickAction, String middleClickAction, int slot, ItemStack itemStack) {
            this.guiItems.add(new TubingGuiItem.Builder(null, slot)
                    .withMiddleClickAction(middleClickAction)
                    .withLeftClickAction(leftClickAction)
                    .withRightClickAction(rightClickAction)
                    .withItemStack(itemStack)
                    .build());
            return this;
        }

        @Deprecated
        public Builder addItem(String leftClickAction, String rightClickAction, int slot, ItemStack itemStack) {
            this.guiItems.add(new TubingGuiItem.Builder(null, slot)
                    .withLeftClickAction(leftClickAction)
                    .withRightClickAction(rightClickAction)
                    .withItemStack(itemStack)
                    .build());
            return this;
        }

        @Deprecated
        public Builder addItem(String leftClickAction, int slot, ItemStack itemStack) {
            this.guiItems.add(new TubingGuiItem.Builder(null, slot)
                    .withLeftClickAction(leftClickAction)
                    .withItemStack(itemStack)
                    .build());
            return this;
        }

        public TubingGui build() {
            return new TubingGui(inventory, guiItems, id);
        }
    }
}
