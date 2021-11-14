package be.garagepoort.mcioc.gui.templates;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.common.TubingPluginProvider;
import be.garagepoort.mcioc.gui.model.TubingGui;
import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;
import be.garagepoort.mcioc.gui.templates.xml.style.TubingGuiStyleParser;
import be.garagepoort.mcioc.gui.templates.xml.TubingGuiXmlParser;
import be.garagepoort.mcioc.permissions.TubingPermissionService;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@IocBean
public class GuiTemplateResolver {

    private final TubingPluginProvider tubingPluginProvider;
    private final Configuration freemarkerConfiguration;
    private final TemplateConfigResolver templateConfigResolver;
    private final DefaultObjectWrapper defaultObjectWrapper;
    private final TubingPermissionService tubingPermissionService;
    private final TubingGuiXmlParser tubingGuiXmlParser;
    private final TubingGuiTemplateParser tubingGuiTemplateParser;
    private final TubingGuiStyleParser tubingGuiStyleParser;

    public GuiTemplateResolver(TubingPluginProvider tubingPluginProvider, TemplateConfigResolver templateConfigResolver, TubingPermissionService tubingPermissionService, TubingGuiXmlParser tubingGuiXmlParser, TubingGuiTemplateParser tubingGuiTemplateParser, TubingGuiStyleParser tubingGuiStyleParser) {
        this.tubingPluginProvider = tubingPluginProvider;
        this.templateConfigResolver = templateConfigResolver;
        this.tubingPermissionService = tubingPermissionService;
        this.tubingGuiXmlParser = tubingGuiXmlParser;
        this.tubingGuiTemplateParser = tubingGuiTemplateParser;
        this.tubingGuiStyleParser = tubingGuiStyleParser;
        freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_28);
        defaultObjectWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_28);
        freemarkerConfiguration.setClassForTemplateLoading(GuiTemplateResolver.this.tubingPluginProvider.getPlugin().getClass(), "/");
    }


    public TubingGui resolve(Player player, String templatePath, Map<String, Object> params) {
        try {
            Template template = freemarkerConfiguration.getTemplate(templatePath);
            TemplateModel statics = defaultObjectWrapper.getStaticModels();
            StringWriter stringWriter = new StringWriter();

            params.put("statics", statics);
            params.put("$config", templateConfigResolver);
            params.put("$permissions", tubingPermissionService);
            template.process(params, stringWriter);
            String templateHtml = stringWriter.toString();
            templateHtml = tubingGuiTemplateParser.parseHtml(templateHtml);
            TubingGui tubingGui = tubingGuiXmlParser.parseHtml(player, templateHtml);
            tubingGuiStyleParser.parse(tubingGui);
            return tubingGui;
        } catch (IOException | TemplateException e) {
            throw new TubingGuiException("Could not load template: [" + templatePath + "]", e);
        }
    }

}
