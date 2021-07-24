package be.garagepoort.mcioc.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TubingGui {

    private final Inventory inventory;
    private final Map<Integer, String> actions;

    public TubingGui(Inventory inventory, Map<Integer, String> actions) {
        this.inventory = inventory;
        this.actions = actions;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Map<Integer, String> getActions() {
        return actions;
    }

    public static class Builder {
        protected final Inventory inventory;
        protected final Map<Integer, String> actions = new HashMap<>();

        public Builder(String title, int size) {
            this.inventory = Bukkit.createInventory(null, size, title);
        }

        public Builder addItem(String action, int slot, ItemStack itemStack) {
            actions.put(slot, action);
            inventory.setItem(slot, itemStack);
            return this;
        }

        public TubingGui build() {
            return new TubingGui(inventory, actions);
        }
    }
}
