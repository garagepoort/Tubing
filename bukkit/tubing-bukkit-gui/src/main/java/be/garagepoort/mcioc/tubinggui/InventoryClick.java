package be.garagepoort.mcioc.tubinggui;

import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.load.InjectTubingPlugin;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitListener;
import be.garagepoort.mcioc.tubinggui.model.TubingGui;
import be.garagepoort.mcioc.tubinggui.model.TubingGuiActions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@IocBukkitListener
public class InventoryClick implements Listener {

    private final TubingPlugin tubingPlugin;
    private final GuiActionService guiActionService;

    public InventoryClick(@InjectTubingPlugin TubingPlugin tubingPlugin, GuiActionService guiActionService) {
        this.tubingPlugin = tubingPlugin;
        this.guiActionService = guiActionService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if (event.getClickedInventory() != null) {
            Optional<TubingGui> inventory = guiActionService.getTubingGui(player);
            inventory.ifPresent(tubingGui -> {
                if (tubingGui.getInteractableSlots().contains(event.getSlot())) {
                    return;
                }

                Inventory clickedInventory = event.getClickedInventory();
                Inventory otherInventory = event.getInventory();

                if (clickedInventory.equals(tubingGui.getInventory())) {
                    String action = getClickAction(event, slot, tubingGui);
                    if (isNotBlank(action) && !action.equals(TubingGuiActions.NOOP)) {
                        guiActionService.executeAction(player, action);
                    }
                    event.setCancelled(true);
                } else if (otherInventory.equals(tubingGui.getInventory())) {
                    if (event.getClick().isShiftClick() || event.getClick().isKeyboardClick() || event.getClick() == ClickType.DOUBLE_CLICK) {
                        event.setCancelled(true);
                    }
                }
            });
        }
    }

    private String getClickAction(InventoryClickEvent event, int slot, TubingGui tubingGui) {
        String action = null;
        if (event.getClick().isLeftClick() && !event.getClick().isShiftClick()) {
            action = tubingGui.getLeftActions().get(slot);
        } else if (event.getClick().isRightClick() && !event.getClick().isShiftClick()) {
            action = tubingGui.getRightActions().get(slot);
        } else if (isLeftShiftClick(event)) {
            action = tubingGui.getLeftShiftActions().get(slot);
        } else if (isRightShiftClick(event)) {
            action = tubingGui.getRightShiftActions().get(slot);
        } else if (event.getClick() == ClickType.MIDDLE) {
            action = tubingGui.getMiddleActions().get(slot);
        }
        return action;
    }

    private boolean isRightShiftClick(InventoryClickEvent event) {
        return event.getClick().isRightClick() && event.getClick().isShiftClick();
    }

    private boolean isLeftShiftClick(InventoryClickEvent event) {
        return event.getClick().isLeftClick() && event.getClick().isShiftClick();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void dragItem(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        GuiActionService actionService = tubingPlugin.getIocContainer().get(GuiActionService.class);

        Optional<TubingGui> inventory = actionService.getTubingGui(player);
        inventory.ifPresent(tubingGui -> {
            Inventory topInventory = event.getView().getTopInventory();
            if (topInventory.equals(event.getInventory()) && topInventory.equals(tubingGui.getInventory())) {
                if (!tubingGui.getInteractableSlots().containsAll(event.getInventorySlots())) {
                    event.setCancelled(true);
                }
            }
        });
    }
}
