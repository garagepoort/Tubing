package be.garagepoort.mcioc.gui.templates.xml;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.model.TubingGuiItemText;
import be.garagepoort.mcioc.gui.style.StyleConfig;

import java.util.Optional;

@IocBean
public class TextStyleParser {

    private final StyleRepository styleRepository;

    public TextStyleParser(StyleRepository styleRepository) {
        this.styleRepository = styleRepository;
    }

    public void parse(TubingGuiItemText tubingGuiItemText) {
        if (tubingGuiItemText.getId() == null) {
            return;
        }

        Optional<StyleConfig> style = styleRepository.getStyleConfigById(tubingGuiItemText.getId());
        if (style.isPresent()) {
            if (style.get().isHidden().isPresent()) {
                tubingGuiItemText.setHidden(style.get().isHidden().get());
            }
            if (style.get().getColor().isPresent()) {
                tubingGuiItemText.setColor(style.get().getColor().get());
            }
        }
    }

}
