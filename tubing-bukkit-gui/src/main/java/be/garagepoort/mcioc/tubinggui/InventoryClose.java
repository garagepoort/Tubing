package be.garagepoort.mcioc.tubinggui;

import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.load.InjectTubingPlugin;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitListener;
import be.garagepoort.mcioc.tubinggui.history.GuiHistoryStack;
import be.garagepoort.mcioc.tubinggui.model.TubingGui;
import be.garagepoort.mcioc.tubinggui.model.TubingGuiActions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@IocBukkitListener
public class InventoryClose implements Listener {
    private final TubingPlugin tubingPlugin;
    private final GuiActionService actionService;
    private final GuiHistoryStack historyStack;

    public InventoryClose(@InjectTubingPlugin TubingPlugin tubingPlugin,
                          GuiActionService guiActionService,
                          GuiHistoryStack guiHistoryStack) {
        this.tubingPlugin = tubingPlugin;
        this.actionService = guiActionService;
        this.historyStack = guiHistoryStack;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();

        Optional<TubingGui> tubingGui = actionService.getTubingGui(player);
        if (tubingGui.isPresent() && event.getInventory().equals(tubingGui.get().getInventory())) {

            String closeAction = tubingGui.get().getCloseAction();

            if (!actionService.isOpeningInventory.getOrDefault(player.getUniqueId(), false)) {
                actionService.isOpeningInventory.put(player.getUniqueId(), false);
                if (closeActionDefined(closeAction) && !historyStack.isLastAction(player.getUniqueId(), closeAction)) {
                    actionService.executeAction(player, closeAction);
                    return;
                }
                historyStack.clear(player.getUniqueId());
            }

            actionService.removeInventory(player);
        }
    }

    private boolean closeActionDefined(String closeAction) {
        return isNotBlank(closeAction) && !closeAction.equals(TubingGuiActions.NOOP);
    }
}
