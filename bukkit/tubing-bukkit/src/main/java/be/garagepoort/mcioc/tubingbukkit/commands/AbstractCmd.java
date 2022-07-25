package be.garagepoort.mcioc.tubingbukkit.commands;

import be.garagepoort.mcioc.tubingbukkit.commands.exceptions.CommandException;
import be.garagepoort.mcioc.tubingbukkit.commands.exceptions.CommandExceptionHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class AbstractCmd implements CommandExecutor {

    private final CommandExceptionHandler commandExceptionHandler;

    protected AbstractCmd(CommandExceptionHandler commandExceptionHandler) {
        this.commandExceptionHandler = commandExceptionHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        try {
            validateMinimumArguments(sender, args);

            return executeCmd(sender, alias, args);
        } catch (Throwable e) {
            commandExceptionHandler.handle(sender, e);
            return false;
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
