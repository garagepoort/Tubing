package be.garagepoort.mcioc.gui.model;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TubingGui {

    private final String id;
    private boolean build;
    private String title;
    private int size;
    private Inventory inventory;
    private final Map<Integer, TubingGuiItem> guiItems = new HashMap<>();

    public TubingGui(List<TubingGuiItem> guiItems, String id, String title, int size) {
        this.id = id;
        this.size = size;
        this.title = title;
        for (TubingGuiItem guiItem : guiItems) {
            this.guiItems.put(guiItem.getSlot(), guiItem);
        }
    }

    public void build() {
        this.inventory = Bukkit.createInventory(null, size, title);
        for (TubingGuiItem guiItem : guiItems.values()) {
            inventory.setItem(guiItem.getSlot(), guiItem.getTubingGuiItemStack().toItemStack());
        }
        this.build = true;
    }

    public Map<Integer, TubingGuiItem> getGuiItems() {
        return guiItems;
    }

    public Inventory getInventory() {
        if (!build) {
            build();
        }
        return inventory;
    }

    public String getId() {
        return id + "_inventory";
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

    public void setSize(int size) {
        this.size = size;
    }

    public static class Builder {
        private String id;
        private final String title;
        private final int size;
        private final List<TubingGuiItem> guiItems = new ArrayList<>();

        public Builder(String title, int size) {
            this.title = title;
            this.size = size;
        }

        public Builder(String title, int size, String guiId) {
            this.title = title;
            this.size = size;
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
                    .withItemStack(mapToTubingItemStack(itemStack))
                    .build());
            return this;
        }

        @Deprecated
        public Builder addItem(String leftClickAction, String rightClickAction, int slot, ItemStack itemStack) {
            this.guiItems.add(new TubingGuiItem.Builder(null, slot)
                    .withLeftClickAction(leftClickAction)
                    .withRightClickAction(rightClickAction)
                    .withItemStack(mapToTubingItemStack(itemStack))
                    .build());
            return this;
        }

        @Deprecated
        public Builder addItem(String leftClickAction, int slot, ItemStack itemStack) {
            this.guiItems.add(new TubingGuiItem.Builder(null, slot)
                    .withLeftClickAction(leftClickAction)
                    .withItemStack(mapToTubingItemStack(itemStack))
                    .build());
            return this;
        }

        private TubingGuiItemStack mapToTubingItemStack(ItemStack itemStack) {
            return new TubingGuiItemStack(itemStack.getType(), new TubingGuiItemText(itemStack.getItemMeta().getDisplayName(), null), false, itemStack.getItemMeta().getLore().stream().map(l -> {
                ItemStackLoreLine itemStackLoreLine = new ItemStackLoreLine();
                itemStackLoreLine.addPart(new TubingGuiItemText(l, null));
                return itemStackLoreLine;
            }).collect(Collectors.toList()));
        }

        public TubingGui build() {
            return new TubingGui(guiItems, id, title, size);
        }
    }
}
