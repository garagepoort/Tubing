package be.garagepoort.mcioc.tubingbukkit.compatibility.messages.v1_12_1;

import be.garagepoort.mcioc.tubingbukkit.compatibility.messages.JSONMessage;
import be.garagepoort.mcioc.tubingbukkit.compatibility.messages.JsonSender;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class JsonSender1_12 implements JsonSender {

    @Override
    public void send(JSONMessage jsonMessage, Player... players) {
        PacketPlayOutChat packet = createTextPacket(jsonMessage.toString());

        for (Player player : players) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    private PacketPlayOutChat createTextPacket(String message) {
        try {
            return new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(message));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
