package be.garagepoort.mcioc.gui;


import be.garagepoort.mcioc.TubingPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
                    String action = event.getClick().isLeftClick() ? tubingGui.getLeftActions().get(slot) : tubingGui.getRightActions().get(slot);
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