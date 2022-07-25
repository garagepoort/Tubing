package be.garagepoort.mcioc.tubingbukkit.commands.exceptions;

import be.garagepoort.mcioc.ConditionalOnMissingBean;
import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.configuration.ConfigurationLoader;
import be.garagepoort.mcioc.tubingbukkit.messaging.Messages;
import org.bukkit.command.CommandSender;

@IocBean
@ConditionalOnMissingBean
public class DefaultCommandExceptionHandler implements CommandExceptionHandler {

    private final ConfigurationLoader configurationLoader;
    private final Messages messages;

    public DefaultCommandExceptionHandler(ConfigurationLoader configurationLoader, Messages messages) {
        this.configurationLoader = configurationLoader;
        this.messages = messages;
    }

    @Override
    public void handle(CommandSender commandSender, Throwable commandException) {
        String message = configurationLoader.getConfigStringValue(commandException.getMessage()).orElse(commandException.getMessage());
        messages.send(commandSender, message);
    }
}
