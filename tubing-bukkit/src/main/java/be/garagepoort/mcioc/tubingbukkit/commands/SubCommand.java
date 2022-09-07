package be.garagepoort.mcioc.tubingbukkit.commands;

import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitSubCommand;
import be.garagepoort.mcioc.tubingbukkit.exceptions.TubingBukkitException;
import be.garagepoort.mcioc.tubingbukkit.permissions.NoPermissionException;
import be.garagepoort.mcioc.tubingbukkit.permissions.TubingPermissionService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface SubCommand {

    default void onCommand(CommandSender sender, String[] args, TubingPermissionService permissionService) {
        validateOnlyPlayers(sender);
        validatePermission(sender,  permissionService);
        executeCmd(sender, args);
    }
    boolean executeCmd(CommandSender sender, String[] args);

    String getHelp();

    default void validatePermission(CommandSender sender, TubingPermissionService permissionService) {
        if (this.getClass().isAnnotationPresent(IocBukkitSubCommand.class)) {
            String permission = this.getClass().getAnnotation(IocBukkitSubCommand.class).permission();
            if (!permission.isEmpty() && !permissionService.has(sender, permission)) {
                throw new NoPermissionException("You don't have permission to execute this command");
            }
        }
    }

    default void validateOnlyPlayers(CommandSender sender) {
        boolean onlyPlayers = false;
        if(this.getClass().isAnnotationPresent(IocBukkitSubCommand.class)) {
            onlyPlayers = this.getClass().getAnnotation(IocBukkitSubCommand.class).onlyPlayers();
        }

        if (onlyPlayers && !(sender instanceof Player)) {
            throw new TubingBukkitException("Can only be executed by Players");
        }
    }
}
