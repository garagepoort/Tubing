package be.garagepoort.mcioc.gui.model;

import be.garagepoort.mcioc.IocBean;
import org.bukkit.ChatColor;

import java.util.Optional;
import java.util.stream.Collectors;

@IocBean
public class TextMapper {

    public Optional<String> mapText(TubingGuiText tubingGuiText) {
        if (tubingGuiText.getParts().stream().allMatch(TubingGuiTextPart::isHidden)) {
            return Optional.empty();
        }

        String collectedParts = tubingGuiText.getParts().stream()
                .filter(p -> !p.isHidden())
                .map(TubingGuiTextPart::toString)
                .collect(Collectors.joining());
        return Optional.of(tubingGuiText.getColor() == null ? this.format(collectedParts) : this.format(tubingGuiText.getColor() + collectedParts));
    }


    private String format(String loreLine) {
        return ChatColor.translateAlternateColorCodes('&', loreLine);
    }
}
