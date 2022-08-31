package be.garagepoort.mcioc.tubingbukkit.messaging;

import be.garagepoort.mcioc.IocBean;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

@IocBean
public class Messages {

    private static final String NO_PREFIX = "[NO_PREFIX]";

    private final PlaceholderService placeholderService;
    private final MessagePrefixProvider messagePrefixProvider;

    public Messages(PlaceholderService placeholderService, MessagePrefixProvider messagePrefixProvider) {
        this.placeholderService = placeholderService;
        this.messagePrefixProvider = messagePrefixProvider;
    }

    public String colorize(String message) {
        message = message.replace("&&", "<ampersand>");
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message.replace("<ampersand>", "&");
    }

    public String parse(Player player, String message) {
        if(isEmpty(message)) {
            return "";
        }
        return colorize(placeholderService.setPlaceholders(player, message));
    }

    public void sendNoPrefix(CommandSender sender, String message) {
        if(isEmpty(message)) {
            return;
        }
        message = placeholderService.setPlaceholders(sender, message);
        for (String s : message.split("\\n")) {
            sender.sendMessage(buildMessage("", s));
        }
    }
    public void send(CommandSender sender, String message) {
        if(isEmpty(message)) {
            return;
        }
        message = placeholderService.setPlaceholders(sender, message);
        for (String s : message.split("\\n")) {
            sender.sendMessage(buildMessage(getPrefix(), s));
        }
    }

    public void broadcast(String message) {
        if(isEmpty(message)) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(receiver -> send(receiver, message));
    }


    public void send(Collection<? extends Player> receivers, String message) {
        if(isEmpty(message)) {
            return;
        }
        receivers.forEach(receiver -> send(receiver, message));
    }

    public void send(Player player, String message, String permission) {
        if(isEmpty(message)) {
            return;
        }
        if (!player.hasPermission(permission)) {
            return;
        }

        send(player, message);
    }

    public void send(CommandSender sender, List<String> messageLines) {
        messageLines.forEach(message -> this.send(sender, message));
    }

    public void sendGlobalMessage(String message) {
        Bukkit.broadcastMessage(buildMessage(getPrefix(), message));
    }

    public void sendGroupMessage(String message, String permission) {
        if(isEmpty(message)) {
            return;
        }
        Bukkit.getOnlinePlayers()
            .forEach(player -> send(player, message, permission));
    }

    private String buildMessage(String prefix, String message) {
        if(message.startsWith(NO_PREFIX)) {
            prefix = "";
            message = message.replace(NO_PREFIX, "");
        }

        if (StringUtils.isEmpty(prefix)) {
            return colorize(message);
        } else {
            return colorize(prefix + " " + message);
        }
    }

    private String getPrefix() {
        return messagePrefixProvider.getPrefix();
    }

    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
