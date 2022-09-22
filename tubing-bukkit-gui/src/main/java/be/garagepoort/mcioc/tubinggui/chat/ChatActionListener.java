package be.garagepoort.mcioc.tubinggui.chat;

import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitListener;
import be.garagepoort.mcioc.tubingbukkit.messaging.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@IocBukkitListener
public class ChatActionListener implements Listener {

    private final ChatActionService chatActionService;
    private final Messages messages;

    public ChatActionListener(ChatActionService chatActionService, Messages messages) {
        this.chatActionService = chatActionService;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent chatEvent) {
        Map<UUID, Consumer<String>> actions = chatActionService.getActions();
        Player player = chatEvent.getPlayer();
        if(!actions.containsKey(player.getUniqueId())) {
            return;
        }
        Consumer<String> chatAction = actions.get(player.getUniqueId());

        try {
            chatAction.accept(chatEvent.getMessage());
            actions.remove(player.getUniqueId());
        } catch (Exception e) {
            messages.send(player, e.getMessage());
        }
        chatEvent.setCancelled(true);
    }
}
