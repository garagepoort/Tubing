package be.garagepoort.mcioc.tubinggui.templates.xml;

import be.garagepoort.mcioc.tubinggui.exceptions.TubingGuiException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

public class MaterialUrlParser {
    public static void updateItemsStackWithCustomTexture(ItemStack head, final String textureUrl) {
        final SkullMeta headMeta = (SkullMeta)head.getItemMeta();
        final GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        final byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", textureUrl).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            throw new TubingGuiException(e);
        }
        head.setItemMeta(headMeta);
    }
}
