package be.garagepoort.mcioc.gui;

import be.garagepoort.mcioc.TubingPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryClose implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        GuiActionService actionService = TubingPlugin.getPlugin().getIocContainer().get(GuiActionService.class);
        actionService.getTubingGui(player).ifPresent(tubingGui -> {
            actionService.removeInventory(player);
        });
    }
}