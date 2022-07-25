package be.garagepoort.mcioc.tubingbukkit.compatibility.messages;

import org.bukkit.entity.Player;

public interface JsonSender {
    void send(JSONMessage jsonMessage, Player... players);
}
