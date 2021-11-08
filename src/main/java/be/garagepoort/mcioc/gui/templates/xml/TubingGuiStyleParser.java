package be.garagepoort.mcioc.gui.templates.xml;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.model.TubingGui;
import be.garagepoort.mcioc.gui.model.TubingGuiItem;
import be.garagepoort.mcioc.gui.style.StyleConfig;

import java.util.Optional;

@IocBean
public class TubingGuiStyleParser {

    private final StyleRepository styleRepository;
    private final TubingGuiItemStyleParser tubingGuiItemStyleParser;

    public TubingGuiStyleParser(StyleRepository styleRepository, TubingGuiItemStyleParser tubingGuiItemStyleParser) {
        this.styleRepository = styleRepository;
        this.tubingGuiItemStyleParser = tubingGuiItemStyleParser;
    }

    public void parse(TubingGui tubingGui) {
        if (tubingGui.getId() == null) {
            return;
        }

        Optional<StyleConfig> style = styleRepository.getStyleConfig(tubingGui.getId());
        if (style.isPresent()) {
            if (style.get().getSize().isPresent()) {
                tubingGui.setSize(style.get().getSize().get());
            }
        }
        for (TubingGuiItem value : tubingGui.getGuiItems().values()) {
            tubingGuiItemStyleParser.parse(value);
        }
    }
}
