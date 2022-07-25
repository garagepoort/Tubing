package be.garagepoort.mcioc.tubingbukkit.compatibility.messages.v1_17_1;

import be.garagepoort.mcioc.tubingbukkit.compatibility.messages.JSONMessage;
import be.garagepoort.mcioc.tubingbukkit.compatibility.messages.JsonSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JsonSender1_17 implements JsonSender {

    @Override
    public void send(JSONMessage jsonMessage, Player...players) {
        sendPacket(createTextPacket(jsonMessage.toString()), players);
    }

    private void sendPacket(ClientboundChatPacket packet, Player... players) {
        if (packet == null) {
            return;
        }

        for (Player player : players) {
            try {
                ((CraftPlayer) player).getHandle().connection.connection.send(packet);
            } catch (Exception e) {
                System.err.println("Failed to send packet");
                e.printStackTrace();
            }
        }
    }

    private ClientboundChatPacket createTextPacket(String message) {
        try {
            ClientboundChatPacket packet = new ClientboundChatPacket(Component.Serializer.fromJson(message), ChatType.CHAT, UUID.randomUUID());
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
