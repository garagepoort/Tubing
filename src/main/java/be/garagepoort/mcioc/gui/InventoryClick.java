package be.garagepoort.mcioc.gui;


import be.garagepoort.mcioc.TubingPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.Optional;

public class InventoryClick implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        GuiActionService actionService = TubingPlugin.getPlugin().getIocContainer().get(GuiActionService.class);

        if (event.getClickedInventory() != null) {
            Optional<TubingGui> inventory = actionService.getTubingGui(player);
            inventory.ifPresent(tubingGui -> {
                if (event.getClickedInventory().equals(tubingGui.getInventory())) {
                    String action = getClickAction(event, slot, tubingGui);

                    if (StringUtils.isNotBlank(action)) {
                        if (action.equals(TubingGuiActions.NOOP)) {
                            event.setCancelled(true);
                            return;
                        }

                        actionService.executeAction(player, action);
                    }
                    event.setCancelled(true);
                }
            });
        }
    }

    private String getClickAction(InventoryClickEvent event, int slot, TubingGui tubingGui) {
        String action = null;
        if (event.getClick().isLeftClick()) {
            action = tubingGui.getLeftActions().get(slot);
        } else if (event.getClick().isRightClick()) {
            action = tubingGui.getRightActions().get(slot);
        } else if (event.getClick() == ClickType.MIDDLE) {
            action = tubingGui.getMiddleActions().get(slot);
        }
        return action;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dragItem(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        GuiActionService actionService = TubingPlugin.getPlugin().getIocContainer().get(GuiActionService.class);

        Optional<TubingGui> inventory = actionService.getTubingGui(player);
        inventory.ifPresent(tubingGui -> {
            if (event.getInventory().equals(tubingGui.getInventory())) {
                event.setCancelled(true);
            }
        });
    }
}