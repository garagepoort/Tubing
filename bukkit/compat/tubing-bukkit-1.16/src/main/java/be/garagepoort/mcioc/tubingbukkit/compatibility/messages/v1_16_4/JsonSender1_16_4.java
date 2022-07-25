package be.garagepoort.mcioc.tubingbukkit.compatibility.messages.v1_16_4;

import be.garagepoort.mcioc.tubingbukkit.compatibility.messages.JSONMessage;
import be.garagepoort.mcioc.tubingbukkit.compatibility.messages.JsonSender;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JsonSender1_16_4 implements JsonSender {

    @Override
    public void send(JSONMessage jsonMessage, Player... players) {
        PacketPlayOutChat packet = createTextPacket(jsonMessage.toString());

        for (Player player : players) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    private PacketPlayOutChat createTextPacket(String message) {
        try {
            return new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(message), ChatMessageType.CHAT, UUID.randomUUID());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
