package be.garagepoort.mcioc.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TubingGui {

    private final Inventory inventory;
    private final Map<Integer, String> leftClickActions;
    private final Map<Integer, String> rightClickActions;

    public TubingGui(Inventory inventory, Map<Integer, String> leftClickActions, Map<Integer, String> rightClickActions) {
        this.inventory = inventory;
        this.leftClickActions = leftClickActions;
        this.rightClickActions = rightClickActions;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Map<Integer, String> getLeftActions() {
        return leftClickActions;
    }

    public Map<Integer, String> getRightActions() {
        return rightClickActions;
    }

    public static class Builder {
        protected final Inventory inventory;
        private final Map<Integer, String> leftClickActions = new HashMap<>();
        private final Map<Integer, String> rightClickActions = new HashMap<>();

        public Builder(String title, int size) {
            this.inventory = Bukkit.createInventory(null, size, title);
        }

        public Builder addItem(String leftClickAction, int slot, ItemStack itemStack) {
            leftClickActions.put(slot, leftClickAction);
            rightClickActions.put(slot, leftClickAction);
            inventory.setItem(slot, itemStack);
            return this;
        }

        public Builder addItem(String leftClickAction, String rightClickAction, int slot, ItemStack itemStack) {
            leftClickActions.put(slot, leftClickAction);
            rightClickActions.put(slot, rightClickAction);
            inventory.setItem(slot, itemStack);
            return this;
        }

        public TubingGui build() {
            return new TubingGui(inventory, leftClickActions, rightClickActions);
        }
    }
}
