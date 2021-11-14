package be.garagepoort.mcioc.gui.templates.xml.style;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.model.TubingGuiItem;
import be.garagepoort.mcioc.gui.model.TubingGuiText;
import be.garagepoort.mcioc.gui.style.StyleConfig;

import java.util.Optional;

@IocBean
public class TubingGuiItemStyleParser {

    private final StyleRepository styleRepository;
    private final TubingGuiTextStyleParser tubingGuiTextStyleParser;

    public TubingGuiItemStyleParser(StyleRepository styleRepository, TubingGuiTextStyleParser tubingGuiTextStyleParser) {
        this.styleRepository = styleRepository;
        this.tubingGuiTextStyleParser = tubingGuiTextStyleParser;
    }

    public void parse(TubingGuiItem tubingGuiItem) {
        if (!tubingGuiItem.getStyleId().isPresent()) {
            return;
        }

        Optional<StyleConfig> style = styleRepository.getStyleConfigById(tubingGuiItem.getStyleId().get());
        if (style.isPresent()) {
            if (style.get().isHidden().isPresent()) {
                tubingGuiItem.setHidden(style.get().isHidden().get());
            }
            if (style.get().isEnchanted().isPresent()) {
                tubingGuiItem.getTubingGuiItemStack().setEnchanted(style.get().isEnchanted().get());
            }
            if (style.get().getSlot().isPresent()) {
                tubingGuiItem.setSlot(style.get().getSlot().get());
            }
            if (style.get().getMaterial().isPresent()) {
                tubingGuiItem.getTubingGuiItemStack().setMaterial(style.get().getMaterial().get());
            }
        }
        tubingGuiTextStyleParser.parse(tubingGuiItem.getTubingGuiItemStack().getName());
        for (TubingGuiText loreLine : tubingGuiItem.getTubingGuiItemStack().getLoreLines()) {
            tubingGuiTextStyleParser.parse(loreLine);
        }
    }

}
