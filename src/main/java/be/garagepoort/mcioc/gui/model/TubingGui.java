package be.garagepoort.mcioc.gui.model;

import be.garagepoort.mcioc.gui.templates.xml.style.StyleId;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TubingGui {

    private StyleId styleId;
    private final TubingGuiText title;
    private int size;
    private Inventory inventory;
    private final Map<Integer, TubingGuiItem> guiItems = new HashMap<>();

    public TubingGui(StyleId styleId, List<TubingGuiItem> guiItems, TubingGuiText title, int size) {
        this.size = size;
        this.title = title;
        if (styleId != null) {
            this.styleId = styleId;
        }
        for (TubingGuiItem guiItem : guiItems) {
            this.guiItems.put(guiItem.getSlot(), guiItem);
        }
    }

    public int getSize() {
        return size;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Map<Integer, TubingGuiItem> getGuiItems() {
        return guiItems;
    }

    public Optional<StyleId> getId() {
        return Optional.ofNullable(styleId);
    }

    public Map<Integer, String> getLeftActions() {
        Map<Integer, String> actions = new HashMap<>();
        for (TubingGuiItem guiItem : guiItems.values()) {
            if (!guiItem.isHidden()) {
                actions.put(guiItem.getSlot(), guiItem.getLeftClickAction());
            }
        }
        return actions;
    }

    public Map<Integer, String> getRightActions() {
        Map<Integer, String> actions = new HashMap<>();
        for (TubingGuiItem guiItem : guiItems.values()) {
            if (!guiItem.isHidden()) {
                actions.put(guiItem.getSlot(), guiItem.getRightClickAction());
            }
        }
        return actions;
    }

    public Map<Integer, String> getMiddleActions() {
        Map<Integer, String> actions = new HashMap<>();
        for (TubingGuiItem guiItem : guiItems.values()) {
            if (!guiItem.isHidden()) {
                actions.put(guiItem.getSlot(), guiItem.getMiddleClickAction());
            }
        }
        return actions;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public TubingGuiText getTitle() {
        return title;
    }

    public static class Builder {
        private StyleId guiId;
        private final TubingGuiText title;
        private final int size;
        private final List<TubingGuiItem> guiItems = new ArrayList<>();

        public Builder(String title, int size) {
            TubingGuiText tubingGuiText = new TubingGuiText();
            tubingGuiText.addPart(new TubingGuiTextPart(title, null));
            this.title = tubingGuiText;
            this.size = size;
        }
        public Builder(TubingGuiText title, int size) {
            this.title = title;
            this.size = size;
        }

        public Builder(StyleId guiId, TubingGuiText title, int size) {
            this.guiId = guiId;
            this.title = title;
            this.size = size;
        }

        public Builder(StyleId guiId, String title, int size) {
            TubingGuiText tubingGuiText = new TubingGuiText();
            tubingGuiText.addPart(new TubingGuiTextPart(title, null));
            this.title = tubingGuiText;
            this.guiId = guiId;
            this.size = size;
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
            TubingGuiText name = new TubingGuiText();
            name.addPart(new TubingGuiTextPart(itemStack.getItemMeta().getDisplayName(), null));

            return new TubingGuiItemStack(itemStack.getType(), name, false, itemStack.getItemMeta().getLore().stream().map(l -> {
                TubingGuiText itemStackLoreLine = new TubingGuiText();
                itemStackLoreLine.addPart(new TubingGuiTextPart(l, null));
                return itemStackLoreLine;
            }).collect(Collectors.toList()));
        }

        public TubingGui build() {
            return new TubingGui(guiId, guiItems, title, size);
        }
    }
}
