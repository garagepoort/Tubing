package be.garagepoort.mcioc.tubingbukkit.commands.exceptions;

import org.bukkit.command.CommandSender;

public interface CommandExceptionHandler {

    void handle(CommandSender commandSender, Throwable commandException);
}
