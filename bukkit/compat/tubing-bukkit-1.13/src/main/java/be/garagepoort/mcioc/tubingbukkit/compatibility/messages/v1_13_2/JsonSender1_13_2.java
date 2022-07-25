package be.garagepoort.mcioc.tubingbukkit.compatibility.messages.v1_13_2;

import be.garagepoort.mcioc.tubingbukkit.compatibility.messages.JSONMessage;
import be.garagepoort.mcioc.tubingbukkit.compatibility.messages.JsonSender;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class JsonSender1_13_2 implements JsonSender {

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
