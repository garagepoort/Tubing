package be.garagepoort.mcioc.gui.templates.xml.style;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.model.TubingGui;
import be.garagepoort.mcioc.gui.model.TubingGuiItem;
import be.garagepoort.mcioc.gui.style.StyleConfig;

import java.util.Optional;

@IocBean
public class TubingGuiStyleParser {

    private final StyleRepository styleRepository;
    private final TubingGuiItemStyleParser tubingGuiItemStyleParser;
    private final TubingGuiTextStyleParser tubingGuiTextStyleParser;

    public TubingGuiStyleParser(StyleRepository styleRepository, TubingGuiItemStyleParser tubingGuiItemStyleParser, TubingGuiTextStyleParser tubingGuiTextStyleParser) {
        this.styleRepository = styleRepository;
        this.tubingGuiItemStyleParser = tubingGuiItemStyleParser;
        this.tubingGuiTextStyleParser = tubingGuiTextStyleParser;
    }

    public void parse(TubingGui tubingGui) {
        if (tubingGui.getId().isPresent()) {
            Optional<StyleConfig> style = styleRepository.getStyleConfigById(tubingGui.getId().get());
            style.ifPresent(styleConfig -> styleConfig.getSize().ifPresent(tubingGui::setSize));
        }

        tubingGuiTextStyleParser.parse(tubingGui.getTitle());

        for (TubingGuiItem value : tubingGui.getGuiItems().values()) {
            tubingGuiItemStyleParser.parse(value);
        }
    }
}
