package be.garagepoort.mcioc.gui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.List;

@IocBean
public class TubingGuiTemplateParser {
    private static final String CONFIG_PREFIX = "config|";

    private final TemplateConfigResolver templateConfigResolver;

    public TubingGuiTemplateParser(TemplateConfigResolver templateConfigResolver) {
        this.templateConfigResolver = templateConfigResolver;
    }

    public String parseHtml(String html) {
        Document document = Jsoup.parse(html, "", Parser.xmlParser());
        document.outputSettings().indentAmount(0).prettyPrint(false);
        Element tubingGuiElement = document.selectFirst("TubingGui");

        if (tubingGuiElement == null) {
            throw new TubingGuiException("Invalid html template. No TubingGui element found");
        }

        replaceAttributes(tubingGuiElement);

        Elements guiItems = tubingGuiElement.select("GuiItem");
        for (Element guiItem : guiItems) {
            replaceAttributes(guiItem);

            parseLoreLines(guiItem);
        }

        return document.toString();
    }

    private void parseLoreLines(Element guiItem) {
        Element loreElement = guiItem.selectFirst("Lore");
        if (loreElement != null) {
            replaceAttributes(loreElement);
            List<Element> loreLinesElements = loreElement.select("LoreLine");
            for (Element loreLineElement : loreLinesElements) {
                replaceAttributes(loreLineElement);
            }
        }
    }

    private void replaceAttributes(Element loreElement) {
        for (Attribute attribute : loreElement.attributes().asList()) {
            attribute.setValue(getAttrValue(attribute.getValue()));
        }
    }

    private String getAttrValue(String attributeValue) {
        if (attributeValue.startsWith(CONFIG_PREFIX)) {
            String configProperty = attributeValue.replace(CONFIG_PREFIX, "");
            return templateConfigResolver.get(configProperty).toString();
        }
        return attributeValue;
    }

}
