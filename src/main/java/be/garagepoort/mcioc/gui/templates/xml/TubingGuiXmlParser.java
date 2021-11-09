package be.garagepoort.mcioc.gui.templates.xml;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;
import be.garagepoort.mcioc.gui.model.ItemStackLoreLine;
import be.garagepoort.mcioc.gui.model.TubingGui;
import be.garagepoort.mcioc.gui.model.TubingGuiItem;
import be.garagepoort.mcioc.gui.model.TubingGuiItemStack;
import be.garagepoort.mcioc.gui.model.TubingGuiItemText;
import be.garagepoort.mcioc.permissions.TubingPermissionService;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@IocBean
public class TubingGuiXmlParser {
    private static final String IF_ATTR = "if";
    private static final String TEXT_TAG = "t";
    private static final String ON_LEFT_CLICK_ATTR = "onLeftClick";
    private static final String ON_RIGHT_CLICK_ATTR = "onRightClick";
    private static final String ON_MIDDLE_CLICK_ATTR = "onMiddleClick";
    private static final String SLOT_ATTR = "slot";
    private static final String ID_ATTR = "id";
    private static final String COLOR_ATTR = "color";
    private static final String MATERIAL_ATTR = "material";
    private static final String NAME_ATTR = "name";
    private static final String ENCHANTED_ATTR = "enchanted";
    private static final String TRUE = "true";
    private static final String PERMISSION_ATTR = "permission";
    public static final String CLASS_ATTR = "class";

    private final TubingPermissionService tubingPermissionService;

    public TubingGuiXmlParser(TubingPermissionService tubingPermissionService, TextStyleParser textStyleParser) {
        this.tubingPermissionService = tubingPermissionService;
    }

    public TubingGui parseHtml(Player player, String html) {
        Document document = Jsoup.parse(html);
        Element tubingGuiElement = document.selectFirst("TubingGui");

        if (tubingGuiElement == null) {
            throw new TubingGuiException("Invalid html template. No TubingGui element found");
        }

        int size = StringUtils.isBlank(tubingGuiElement.attr("size")) ? 54 : Integer.parseInt(tubingGuiElement.attr("size"));
        Element titleElement = tubingGuiElement.selectFirst("title");
        StyleId guiId = getId(tubingGuiElement);
        TubingPlugin.getPlugin().getLogger().info("GuiId: " + guiId);
        String title = titleElement == null ? "" : titleElement.text();

        TubingGui.Builder builder = new TubingGui.Builder(format(title), size, guiId)
                .withId(guiId);

        Elements guiItems = tubingGuiElement.select("GuiItem");
        for (Element guiItem : guiItems) {
            if (validateShowElement(guiItem, player)) {
                String leftClickAction = guiItem.attr(ON_LEFT_CLICK_ATTR);
                String rightClickAction = guiItem.attr(ON_RIGHT_CLICK_ATTR);
                String middleClickAction = guiItem.attr(ON_MIDDLE_CLICK_ATTR);
                StyleId guiItemId = getId(guiItem);

                int slot = Integer.parseInt(guiItem.attr(SLOT_ATTR));
                String material = guiItem.attr(MATERIAL_ATTR);
                String name = guiItem.attr(NAME_ATTR);
                boolean enchanted = guiItem.hasAttr(ENCHANTED_ATTR);
                List<ItemStackLoreLine> loreLines = parseLoreLines(player, guiItem);

                TubingGuiItemStack itemStack = new TubingGuiItemStack(Material.valueOf(material), new TubingGuiItemText(name, null), enchanted, loreLines);
                TubingGuiItem tubingGuiItem = new TubingGuiItem.Builder(guiItemId, slot)
                        .withLeftClickAction(leftClickAction)
                        .withRightClickAction(rightClickAction)
                        .withMiddleClickAction(middleClickAction)
                        .withItemStack(itemStack)
                        .build();
                builder.addItem(tubingGuiItem);
            }
        }

        return builder.build();
    }

    private StyleId getId(Element element) {
        List<String> classes = new ArrayList<>();
        if (element.hasAttr(CLASS_ATTR)) {
            classes = Arrays.asList(element.attr(CLASS_ATTR).split(" "));
        }

        if (!element.hasParent() && !element.hasAttr(ID_ATTR)) {
            return null;
        }
        if (element.hasParent() && !element.hasAttr(ID_ATTR)) {
            return getId(element.parent());
        }
        if (!element.hasParent() && element.hasAttr(ID_ATTR)) {
            return new StyleId(element.attr("id"), null, classes);
        }

        return new StyleId(element.attr("id"), getId(element.parent()), classes);
    }

    private List<ItemStackLoreLine> parseLoreLines(Player player, Element guiItem) {
        Element loreElement = guiItem.selectFirst("Lore");
        List<ItemStackLoreLine> loreLines = new ArrayList<>();
        if (loreElement != null) {
            if (validateShowElement(loreElement, player)) {
                List<Element> loreLinesElements = loreElement.select("LoreLine").stream()
                        .filter(g -> validateShowElement(g, player))
                        .collect(Collectors.toList());

                loreLines = loreLinesElements.stream()
                        .map(this::parseLoreLine)
                        .collect(Collectors.toList());
            }
        }
        return loreLines;
    }

    private ItemStackLoreLine parseLoreLine(Element loreLine) {
        ItemStackLoreLine itemStackLoreLine = new ItemStackLoreLine();
        if (loreLine.select(TEXT_TAG).isEmpty()) {
            itemStackLoreLine.addPart(new TubingGuiItemText(loreLine.text(), null));
            return itemStackLoreLine;
        }

        for (Element textElement : loreLine.select(TEXT_TAG)) {
            TubingGuiItemText tubingGuiItemText = new TubingGuiItemText(textElement.wholeText(), textElement.attr(COLOR_ATTR));
            StyleId id = getId(textElement);
            tubingGuiItemText.setId(id);
            itemStackLoreLine.addPart(tubingGuiItemText);
        }
        return itemStackLoreLine;
    }

    private boolean validateShowElement(Element guiItem, Player player) {
        return ifCheck(guiItem.attr(IF_ATTR)) && permissionCheck(player, guiItem.attr(PERMISSION_ATTR));
    }

    private boolean ifCheck(String attr) {
        return StringUtils.isBlank(attr) || TRUE.equalsIgnoreCase(attr);
    }

    private boolean permissionCheck(Player player, String attr) {
        return StringUtils.isBlank(attr) || tubingPermissionService.has(player, attr);
    }

    private String format(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
