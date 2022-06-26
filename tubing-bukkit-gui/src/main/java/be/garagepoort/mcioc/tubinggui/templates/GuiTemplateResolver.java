package be.garagepoort.mcioc.tubinggui.templates;

import org.bukkit.entity.Player;

import java.util.Map;

public interface GuiTemplateResolver {
    String resolve(Player player, String templatePath, Map<String, Object> params);
}
