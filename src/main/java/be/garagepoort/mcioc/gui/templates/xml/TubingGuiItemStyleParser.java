package be.garagepoort.mcioc.gui.templates.xml;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.model.ItemStackLoreLine;
import be.garagepoort.mcioc.gui.model.TubingGuiItem;
import be.garagepoort.mcioc.gui.model.TubingGuiItemText;
import be.garagepoort.mcioc.gui.style.StyleConfig;

import java.util.Optional;

@IocBean
public class TubingGuiItemStyleParser {

    private final StyleRepository styleRepository;
    private final TextStyleParser textStyleParser;

    public TubingGuiItemStyleParser(StyleRepository styleRepository, TextStyleParser textStyleParser) {
        this.styleRepository = styleRepository;
        this.textStyleParser = textStyleParser;
    }

    public void parse(TubingGuiItem tubingGuiItem) {
        if (!tubingGuiItem.getId().isPresent()) {
            return;
        }

        Optional<StyleConfig> style = styleRepository.getStyleConfigById(tubingGuiItem.getId().get());
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

        for (ItemStackLoreLine loreLine : tubingGuiItem.getTubingGuiItemStack().getLoreLines()) {
            for (TubingGuiItemText part : loreLine.getParts()) {
                textStyleParser.parse(part);
            }
        }
    }

}
