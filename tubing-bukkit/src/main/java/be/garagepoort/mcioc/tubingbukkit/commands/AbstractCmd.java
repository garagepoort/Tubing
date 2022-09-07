package be.garagepoort.mcioc.tubingbukkit.commands;

import be.garagepoort.mcioc.tubingbukkit.annotations.IocBukkitCommandHandler;
import be.garagepoort.mcioc.tubingbukkit.commands.exceptions.CommandException;
import be.garagepoort.mcioc.tubingbukkit.commands.exceptions.CommandExceptionHandler;
import be.garagepoort.mcioc.tubingbukkit.exceptions.TubingBukkitException;
import be.garagepoort.mcioc.tubingbukkit.permissions.NoPermissionException;
import be.garagepoort.mcioc.tubingbukkit.permissions.TubingPermissionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractCmd implements CommandExecutor {

    private final CommandExceptionHandler commandExceptionHandler;
    private final TubingPermissionService permissionService;

    protected AbstractCmd(CommandExceptionHandler commandExceptionHandler, TubingPermissionService permissionService) {
        this.commandExceptionHandler = commandExceptionHandler;
        this.permissionService = permissionService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        try {
            validateOnlyPlayers(sender);
            validatePermission(sender);
            validateMinimumArguments(sender, args);

            return executeCmd(sender, alias, args);
        } catch (Throwable e) {
            commandExceptionHandler.handle(sender, e);
            return false;
        }
    }

    private void validateOnlyPlayers(CommandSender sender) {
        boolean onlyPlayers = false;
        if(this.getClass().isAnnotationPresent(IocBukkitCommandHandler.class)) {
            onlyPlayers = this.getClass().getAnnotation(IocBukkitCommandHandler.class).onlyPlayers();
        }

        if (onlyPlayers && !(sender instanceof Player)) {
            throw new TubingBukkitException("Can only be executed by Players");
        }
    }
    private void validatePermission(CommandSender sender) {
        String permission = "";
        if(this.getClass().isAnnotationPresent(IocBukkitCommandHandler.class)) {
            permission = this.getClass().getAnnotation(IocBukkitCommandHandler.class).permission();
        }

        if(!permission.isEmpty() && !permissionService.has(sender, permission)) {
            throw new NoPermissionException("You don't have permission to execute this command");
        }
    }

    protected abstract boolean executeCmd(CommandSender sender, String alias, String[] args);

    /**
     * @param sender CommandSender
     * @param args   The original args passed to the command
     * @return Integer indicating the minimum amount of arguments that should be present.
     * if this value is not reached an exception will be thrown
     */
    protected abstract int getMinimumArguments(CommandSender sender, String[] args);

    private void validateMinimumArguments(CommandSender sender, String[] args) {
        if (args.length < getMinimumArguments(sender, args)) {
            throw new CommandException("Arguments invalid.");
        }
    }
}
