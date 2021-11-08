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

        Optional<StyleConfig> style = styleRepository.getStyleConfig(tubingGuiItemText.getId());
        if (style.isPresent()) {
            if (style.get().isHidden()) {
                tubingGuiItemText.setHidden(true);
            }
            if (style.get().getColor().isPresent()) {
                tubingGuiItemText.setColor(style.get().getColor().get());
            }
        }
    }

}
