package be.garagepoort.mcioc.tubinggui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.tubinggui.model.TubingGui;
import be.garagepoort.mcioc.tubinggui.templates.xml.TubingXmlToTubingGuiMapper;
import be.garagepoort.mcioc.tubinggui.templates.xml.style.TubingGuiStyleParser;
import org.bukkit.entity.Player;

import java.util.Map;

@IocBean
public class GuiTemplateProcessor {

    private final GuiTemplateResolver guiTemplateResolver;
    private final TubingXmlConfigParser tubingXmlConfigParser;
    private final TubingXmlToTubingGuiMapper xmlToTubingGuiMapper;
    private final TubingGuiStyleParser tubingGuiStyleParser;

    public GuiTemplateProcessor(GuiTemplateResolver guiTemplateResolver, TubingXmlConfigParser tubingGuiTemplateParser, TubingXmlToTubingGuiMapper tubingGuiXmlParser, TubingGuiStyleParser tubingGuiStyleParser) {
        this.guiTemplateResolver = guiTemplateResolver;
        this.tubingXmlConfigParser = tubingGuiTemplateParser;
        this.xmlToTubingGuiMapper = tubingGuiXmlParser;
        this.tubingGuiStyleParser = tubingGuiStyleParser;
    }

    public TubingGui process(Player player, String templatePath, Map<String, Object> params) {
        String templateHtml = guiTemplateResolver.resolve(player, templatePath, params);
        templateHtml = tubingXmlConfigParser.resolveConfigAttributes(templateHtml);
        TubingGui tubingGui = xmlToTubingGuiMapper.toTubingGui(player, templateHtml);
        tubingGuiStyleParser.parse(tubingGui);
        return tubingGui;
    }
}
