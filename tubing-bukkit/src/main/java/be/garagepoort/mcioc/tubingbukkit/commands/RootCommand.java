package be.garagepoort.mcioc.tubingbukkit.commands;

import be.garagepoort.mcioc.IocMulti;
import be.garagepoort.mcioc.tubingbukkit.commands.exceptions.CommandException;
import be.garagepoort.mcioc.tubingbukkit.commands.exceptions.CommandExceptionHandler;
import be.garagepoort.mcioc.tubingbukkit.messaging.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RootCommand extends AbstractCmd implements TabCompleter {

    protected final List<SubCommand> subCommands;

    private final Messages messages;

    public RootCommand(CommandExceptionHandler commandExceptionHandler,
                       @IocMulti(SubCommand.class) List<SubCommand> subCommands,
                       Messages messages) {
        super(commandExceptionHandler);
        this.subCommands = subCommands.stream().filter(s -> s.getRoot().equalsIgnoreCase(getRootId())).collect(Collectors.toList());
        this.messages = messages;
    }

    protected abstract String getRootId();

    @Override
    protected boolean executeCmd(CommandSender sender, String alias, String[] args) {
        if (args.length < 1) {
            onZeroArguments(sender);
            return true;
        }

        String action = args[0];
        if (action.equalsIgnoreCase("help")) {
            onHelp(sender);
            return true;
        }

        SubCommand subCommand = subCommands.stream().filter(s -> s.getAction().equalsIgnoreCase(action)).findFirst().orElseThrow(() -> new CommandException("Invalid command action"));
        subCommand.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    protected void onHelp(CommandSender sender) {
        messages.send(sender, "&2" + getRootId() + " help");
        subCommands.stream()
            .sorted(Comparator.comparing(SubCommand::getAction))
            .forEach(subCommand -> messages.send(sender, subCommand.getHelp()));
    }

    protected void onZeroArguments(CommandSender sender) {
        onHelp(sender);
    }

    @Override
    protected int getMinimumArguments(CommandSender sender, String[] args) {
        return 0;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            List<String> result = subCommands.stream().map(SubCommand::getAction).collect(Collectors.toList());
            result.add("help");
            return result;
        }
        return Collections.emptyList();
    }
}
