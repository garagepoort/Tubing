package be.garagepoort.mcioc.tubinggui.model;

import be.garagepoort.mcioc.tubinggui.templates.xml.style.StyleId;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private String closeAction;
    private List<Integer> interactableSlots = new ArrayList<>();
    private Inventory inventory;
    private final Map<Integer, TubingGuiItem> guiItems = new HashMap<>();

    public TubingGui(StyleId styleId, List<TubingGuiItem> guiItems, TubingGuiText title, int size, List<Integer> interactableSlots, String closeAction) {
        this.size = size;
        this.title = title;
        this.interactableSlots = interactableSlots;
        this.closeAction = closeAction;
        if (styleId != null) {
            this.styleId = styleId;
        }
        for (TubingGuiItem guiItem : guiItems) {
            this.guiItems.put(guiItem.getSlot(), guiItem);
        }
    }
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

    public Map<Integer, String> getLeftShiftActions() {
        Map<Integer, String> actions = new HashMap<>();
        for (TubingGuiItem guiItem : guiItems.values()) {
            if (!guiItem.isHidden()) {
                actions.put(guiItem.getSlot(), guiItem.getLeftShiftClickAction());
            }
        }
        return actions;
    }

    public List<ItemStack> getInteractableItems() {
        return getInteractableSlots().stream()
            .map(slot -> inventory.getItem(slot))
            .filter(item -> item != null && item.getType() != Material.AIR)
            .peek(item -> item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType())))
            .collect(Collectors.toList());
    }

    public List<Integer> getInteractableSlots() {
        return interactableSlots;
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

    public Map<Integer, String> getRightShiftActions() {
        Map<Integer, String> actions = new HashMap<>();
        for (TubingGuiItem guiItem : guiItems.values()) {
            if (!guiItem.isHidden()) {
                actions.put(guiItem.getSlot(), guiItem.getRightShiftClickAction());
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

    public void setCloseAction(String closeAction) {
        this.closeAction = closeAction;
    }

    public String getCloseAction() {
        return closeAction;
    }

    public TubingGuiText getTitle() {
        return title;
    }

    public static class Builder {
        private StyleId guiId;
        private final TubingGuiText title;
        private final int size;
        private List<Integer> interactableSlots = new ArrayList<>();
        private final List<TubingGuiItem> guiItems = new ArrayList<>();
        private String closeAction;

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

        public Builder(StyleId guiId, TubingGuiText title, int size, List<Integer> interactableSlots) {
            this.guiId = guiId;
            this.title = title;
            this.size = size;
            this.interactableSlots = interactableSlots;
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

        public Builder closeAction(String closeAction) {
            this.closeAction = closeAction;
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

            return new TubingGuiItemStack(itemStack.getAmount(),
                itemStack.getType(),
                name,
                false,
                itemStack.getItemMeta().getLore().stream().map(l -> {
                    TubingGuiText itemStackLoreLine = new TubingGuiText();
                    itemStackLoreLine.addPart(new TubingGuiTextPart(l, null));
                    return itemStackLoreLine;
                }).collect(Collectors.toList()));
        }

        public TubingGui build() {
            return new TubingGui(guiId, guiItems, title, size, interactableSlots, closeAction);
        }
    }
}
