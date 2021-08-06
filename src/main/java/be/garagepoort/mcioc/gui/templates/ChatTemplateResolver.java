package be.garagepoort.mcioc.gui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.gui.TubingChatGui;
import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@IocBean
public class ChatTemplateResolver {

    private final Configuration freemarkerConfiguration;
    private final DefaultObjectWrapper defaultObjectWrapper;

    public ChatTemplateResolver() {
        freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_28);
        defaultObjectWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_28);
        freemarkerConfiguration.setClassForTemplateLoading(TubingPlugin.getPlugin().getClass(), "/");
    }

    public TubingChatGui resolve(String templatePath) {
        return resolve(templatePath, new HashMap<>());
    }

    public TubingChatGui resolve(String templatePath, Map<String, Object> params) {
        try {
            Template template = freemarkerConfiguration.getTemplate(templatePath);
            TemplateModel statics = defaultObjectWrapper.getStaticModels();
            StringWriter stringWriter = new StringWriter();

            Map<String, FileConfiguration> fileConfigurations = TubingPlugin.getPlugin().getFileConfigurations();
            fileConfigurations.forEach((k, v) -> {
                Set<String> keys = v.getKeys(true);
                for (String key : keys) {
                    params.put(k + ":" + key, v.get(key));
                    if (k.equalsIgnoreCase("config")) {
                        params.put(key, v.get(key));
                    }
                }
            });

            params.put("statics", statics);
            template.process(params, stringWriter);
            return parseHtml(stringWriter.toString());
        } catch (IOException | TemplateException e) {
            throw new TubingGuiException("Could not load template: [" + templatePath + "]", e);
        }
    }

    private TubingChatGui parseHtml(String html) {
        Document document = Jsoup.parse(html);
        Element tubingGuiElement = document.selectFirst("TubingChatGui");
        String prefix = tubingGuiElement.attr("prefix");

        if (tubingGuiElement == null) {
            throw new TubingGuiException("Invalid html template. No TubingChatGui element found");
        }

        TubingChatGui.Builder builder = new TubingChatGui.Builder();
        Elements guiItems = tubingGuiElement.select("ChatLine");
        guiItems.stream()
                .map(e -> format(prefix + e.text()))
                .forEach(builder::addLine);

        return builder.build();
    }

    private String format(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
