package be.garagepoort.mcioc.tubinggui.chat;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitListener;
import be.garagepoort.mcioc.tubingbukkit.messaging.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@IocBean
@IocBukkitListener
public class ChatActionService implements Listener {

    private final Messages messages;
    private final Map<UUID, Consumer<String>> actions = new HashMap<>();

    public ChatActionService(Messages messages) {
        this.messages = messages;
    }

    public void requireInput(Player player, String message, Consumer<String> input) {
        messages.send(player, message);
        actions.put(player.getUniqueId(), input);
    }

    public void requireInput(Player player, Consumer<String> input) {
        actions.put(player.getUniqueId(), input);
    }

    Map<UUID, Consumer<String>> getActions() {
        return actions;
    }
}
