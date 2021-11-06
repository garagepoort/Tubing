package be.garagepoort.mcioc.gui;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class TubingGuiItem {

    private final String id;
    private final int slot;
    private final String leftClickAction;
    private final String rightClickAction;
    private final String middleClickAction;
    private final ItemStack itemStack;

    public TubingGuiItem(String id, int slot, String leftClickAction, String rightClickAction, String middleClickAction, ItemStack itemStack) {
        this.id = id;
        this.slot = slot;
        this.leftClickAction = leftClickAction;
        this.rightClickAction = rightClickAction;
        this.middleClickAction = middleClickAction;
        this.itemStack = itemStack;
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public String getLeftClickAction() {
        return leftClickAction;
    }

    public String getRightClickAction() {
        return rightClickAction;
    }

    public String getMiddleClickAction() {
        return middleClickAction;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static class Builder {
        private final String id;
        private final int slot;
        private String leftClickAction = TubingGuiActions.NOOP;
        private String rightClickAction = TubingGuiActions.NOOP;
        private String middleClickAction = TubingGuiActions.NOOP;
        private ItemStack itemStack;

        public Builder(String id, int slot) {
            this.id = id;
            this.slot = slot;
        }

        public Builder withLeftClickAction(String leftClickAction) {
            this.leftClickAction = leftClickAction;
            return this;
        }

        public Builder withRightClickAction(String rightClickAction) {
            this.rightClickAction = rightClickAction;
            return this;
        }

        public Builder withMiddleClickAction(String middleClickAction) {
            this.middleClickAction = middleClickAction;
            return this;
        }


        public Builder withItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public TubingGuiItem build() {
            return new TubingGuiItem(id, slot, leftClickAction, rightClickAction, middleClickAction, itemStack);
        }
    }
}
