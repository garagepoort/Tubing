package be.garagepoort.mcioc.gui.templates.xml;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.TubingGui;
import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;
import be.garagepoort.mcioc.permissions.TubingPermissionService;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@IocBean
public class TubingGuiXmlParser {
    private static final String IF_ATTR = "if";
    private static final String ON_LEFT_CLICK_ATTR = "onLeftClick";
    private static final String ON_RIGHT_CLICK_ATTR = "onRightClick";
    private static final String ON_MIDDLE_CLICK_ATTR = "onMiddleClick";
    private static final String SLOT_ATTR = "slot";
    private static final String MATERIAL_ATTR = "material";
    private static final String NAME_ATTR = "name";
    private static final String ENCHANTED_ATTR = "enchanted";
    private static final String TRUE = "true";
    private static final String PERMISSION_ATTR = "permission";

    private final TubingPermissionService tubingPermissionService;

    public TubingGuiXmlParser(TubingPermissionService tubingPermissionService) {
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
        String title = titleElement == null ? "" : titleElement.text();

        TubingGui.Builder builder = new TubingGui.Builder(format(title), size);
        Elements guiItems = tubingGuiElement.select("GuiItem");
        for (Element guiItem : guiItems) {
            if (validateShowElement(guiItem, player)) {
                String leftClickAction = guiItem.attr(ON_LEFT_CLICK_ATTR);
                String rightClickAction = guiItem.attr(ON_RIGHT_CLICK_ATTR);
                String middleClickAction = guiItem.attr(ON_MIDDLE_CLICK_ATTR);

                int slot = Integer.parseInt(guiItem.attr(SLOT_ATTR));
                String material = guiItem.attr(MATERIAL_ATTR);
                String name = guiItem.attr(NAME_ATTR);
                boolean enchanted = guiItem.hasAttr(ENCHANTED_ATTR);
                List<String> loreLines = parseLoreLines(player, guiItem);
                builder.addItem(leftClickAction, rightClickAction, middleClickAction, slot, itemStack(material, name, loreLines, enchanted));
            }
        }

        return builder.build();
    }

    private List<String> parseLoreLines(Player player, Element guiItem) {
        Element loreElement = guiItem.selectFirst("Lore");
        List<String> loreLines = new ArrayList<>();
        if (loreElement != null) {
            if (validateShowElement(loreElement, player)) {
                List<Element> loreLinesElements = loreElement.select("LoreLine").stream()
                        .filter(g -> validateShowElement(g, player))
                        .collect(Collectors.toList());

                loreLines = loreLinesElements.stream().map(Element::text).collect(Collectors.toList());
            }
        }
        return loreLines;
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

    private ItemStack itemStack(String material, String name, List<String> lore, boolean enchanted) {
        ItemStack itemStack = new ItemStack(Material.valueOf(material));
        itemStack.setAmount(1);

        addName(itemStack, name);
        addLore(itemStack, lore);

        ItemMeta meta = itemStack.getItemMeta();
        if (enchanted) {
            itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void addName(ItemStack itemStack, String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name.equals("") ? " " : format(name));
        itemStack.setItemMeta(itemMeta);
    }

    private void addLore(ItemStack itemStack, List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> original = itemMeta.getLore();
        if (original == null) original = new ArrayList<>();
        original.addAll(format(lore));
        itemMeta.setLore(original);
        itemStack.setItemMeta(itemMeta);
    }

    private String format(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private List<String> format(List<String> strings) {
        return strings.stream().map(this::format).collect(Collectors.toList());
    }
}
