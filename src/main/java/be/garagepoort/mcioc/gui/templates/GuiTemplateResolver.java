package be.garagepoort.mcioc.gui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.gui.TubingGui;
import be.garagepoort.mcioc.gui.TubingGuiException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@IocBean
public class GuiTemplateResolver {

    private final Configuration freemarkerConfiguration;

    public GuiTemplateResolver() {
        freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_28);
        freemarkerConfiguration.setClassForTemplateLoading(TubingPlugin.getPlugin().getClass(), "/");
    }

    public TubingGui resolve(String templatePath) {
        return resolve(templatePath, new HashMap<>());
    }

    public TubingGui resolve(String templatePath, Map<String, Object> params) {
        try {
            Template template = freemarkerConfiguration.getTemplate(templatePath);
            StringWriter stringWriter = new StringWriter();
            template.process(params, stringWriter);
            return parseHtml(stringWriter.toString());
        } catch (IOException | TemplateException e) {
            throw new TubingGuiException("Could not load template: [" + templatePath + "]", e);
        }
    }

    private TubingGui parseHtml(String html) {
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
            int slot = Integer.parseInt(guiItem.attr("slot"));
            String leftClickAction = guiItem.attr("onLeftClick");
            String rightClickAction = guiItem.attr("onRightClick");
            String material = guiItem.select("Material").text();
            String name = guiItem.select("Name").text();

            Element loreElement = guiItem.selectFirst("Lore");
            List<String> loreLines = new ArrayList<>();
            if (loreElement != null) {
                Elements loreLinesElements = loreElement.select("LoreLine");
                loreLines = loreLinesElements.stream().map(Element::text).collect(Collectors.toList());
            }

            builder.addItem(leftClickAction, rightClickAction, slot, itemStack(material, name, loreLines));
        }

        return builder.build();
    }

    private ItemStack itemStack(String material, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(Material.valueOf(material));
        itemStack.setAmount(1);

        addName(itemStack, name);
        addLore(itemStack, lore);
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
