package be.garagepoort.mcioc.tubingbukkit.messaging;

import be.garagepoort.mcioc.ConditionalOnMissingBean;
import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.configuration.ConfigProperty;

@IocBean
@ConditionalOnMissingBean
public class TubingMessagePrefixProvider implements MessagePrefixProvider {
    @ConfigProperty("messages.prefix")
    private String prefix;

    @Override
    public String getPrefix() {
        return prefix;
    }
}
