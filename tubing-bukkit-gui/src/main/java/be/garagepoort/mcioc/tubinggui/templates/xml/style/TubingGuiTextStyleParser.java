package be.garagepoort.mcioc.tubinggui.templates.xml.style;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.tubinggui.model.TubingGuiText;
import be.garagepoort.mcioc.tubinggui.model.TubingGuiTextPart;
import be.garagepoort.mcioc.tubinggui.style.StyleConfig;

import java.util.Optional;

@IocBean
public class TubingGuiTextStyleParser {

    private final StyleRepository styleRepository;

    public TubingGuiTextStyleParser(StyleRepository styleRepository) {
        this.styleRepository = styleRepository;
    }

    public void parse(TubingGuiText tubingGuiText) {

        for (TubingGuiTextPart part : tubingGuiText.getParts()) {
            if (part.getId() == null) {
                continue;
            }
            Optional<StyleConfig> style = styleRepository.getStyleConfigById(part.getId());
            if (style.isPresent()) {
                if (style.get().isHidden().isPresent()) {
                    part.setHidden(style.get().isHidden().get());
                }
                if (style.get().getColor().isPresent()) {
                    part.setColor(style.get().getColor().get());
                }
            }
        }

    }

}
