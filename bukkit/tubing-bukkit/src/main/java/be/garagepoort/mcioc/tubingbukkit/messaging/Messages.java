package be.garagepoort.mcioc.tubingbukkit.messaging;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.configuration.ConfigProperty;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

@IocBean
public class Messages {

    @ConfigProperty("messages.prefix")
    private String prefix;

    private final PlaceholderService placeholderService;

    public Messages(PlaceholderService placeholderService) {
        this.placeholderService = placeholderService;
    }

    public String colorize(String message) {
        message = message.replace("&&", "<ampersand>");
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message.replace("<ampersand>", "&");
    }

    public String parse(Player player, String message) {
        return colorize(placeholderService.setPlaceholders(player, message));
    }

    public void sendNoPrefix(CommandSender sender, String message) {
        message = placeholderService.setPlaceholders(sender, message);
        for (String s : message.split("\\n")) {
            sender.sendMessage(buildMessage("", s));
        }
    }
    public void send(CommandSender sender, String message) {
        message = placeholderService.setPlaceholders(sender, message);
        for (String s : message.split("\\n")) {
            sender.sendMessage(buildMessage(prefix, s));
        }
    }

    public void send(Collection<? extends Player> receivers, String message) {
        receivers.forEach(receiver -> send(receiver, message));
    }

    public void send(Player player, String message, String permission) {
        if (!player.hasPermission(permission)) {
            return;
        }

        send(player, message);
    }

    public void send(CommandSender sender, List<String> messageLines) {
        messageLines.forEach(message -> this.send(sender, message));
    }

    public void sendGlobalMessage(String message) {
        Bukkit.broadcastMessage(buildMessage(prefix, message));
    }

    public void sendGroupMessage(String message, String permission) {
        if (message == null) {
            return;
        }
        Bukkit.getOnlinePlayers()
            .forEach(player -> send(player, message, permission));
    }

    private String buildMessage(String prefix, String message) {
        if (StringUtils.isEmpty(prefix)) {
            return colorize(message);
        } else {
            return colorize(prefix + " " + message);
        }
    }
}
