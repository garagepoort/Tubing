package be.garagepoort.mcioc.tubingbukkit.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    boolean onCommand(CommandSender sender, String[] args);

    String getHelp();
}
