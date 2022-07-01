package be.garagepoort.mcioc.tubinggui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.configuration.ConfigurationLoader;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;
import be.garagepoort.mcioc.load.InjectTubingPlugin;
import be.garagepoort.mcioc.tubinggui.exceptions.TubingGuiException;
import be.garagepoort.mcioc.tubinggui.model.TubingChatGui;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.bukkit.ChatColor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@IocBean
public class ChatTemplateResolver {

    private final Configuration freemarkerConfiguration;
    private final DefaultObjectWrapper defaultObjectWrapper;
    private final ConfigurationLoader configurationLoader;

    public ChatTemplateResolver(@InjectTubingPlugin TubingPlugin tubingPlugin, ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
        freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_28);
        defaultObjectWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_28);
        freemarkerConfiguration.setClassForTemplateLoading(tubingPlugin.getClass(), "/");
    }

    public TubingChatGui resolve(String templatePath) {
        return resolve(templatePath, new HashMap<>());
    }

    public TubingChatGui resolve(String templatePath, Map<String, Object> params) {
        try {
            Template template = freemarkerConfiguration.getTemplate(templatePath);
            TemplateModel statics = defaultObjectWrapper.getStaticModels();
            StringWriter stringWriter = new StringWriter();

            Map<String, FileConfiguration> fileConfigurations = configurationLoader.getConfigurationFiles();
            fileConfigurations.forEach((k, v) -> {
                Collection<String> keys = v.getKeys(true);
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
