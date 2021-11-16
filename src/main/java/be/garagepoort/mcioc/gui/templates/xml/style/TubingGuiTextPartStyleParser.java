package be.garagepoort.mcioc.gui.templates.xml.style;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.model.TubingGuiTextPart;
import be.garagepoort.mcioc.gui.style.StyleConfig;

import java.util.Optional;

@IocBean
public class TubingGuiTextPartStyleParser {

    private final StyleRepository styleRepository;

    public TubingGuiTextPartStyleParser(StyleRepository styleRepository) {
        this.styleRepository = styleRepository;
    }

    public void parse(TubingGuiTextPart tubingGuiTextPart) {
        if (tubingGuiTextPart.getId() == null) {
            return;
        }

        Optional<StyleConfig> style = styleRepository.getStyleConfigById(tubingGuiTextPart.getId());
        if (style.isPresent()) {
            if (style.get().isHidden().isPresent()) {
                tubingGuiTextPart.setHidden(style.get().isHidden().get());
            }
            if (style.get().getColor().isPresent()) {
                tubingGuiTextPart.setColor(style.get().getColor().get());
            }
        }
    }

}
