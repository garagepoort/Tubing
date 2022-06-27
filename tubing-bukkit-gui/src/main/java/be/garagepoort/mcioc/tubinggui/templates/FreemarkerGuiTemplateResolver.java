package be.garagepoort.mcioc.tubinggui.templates;

import be.garagepoort.mcioc.ConditionalOnMissingBean;
import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.load.InjectTubingPlugin;
import be.garagepoort.mcioc.tubingbukkit.permissions.TubingPermissionService;
import be.garagepoort.mcioc.tubinggui.exceptions.TubingGuiException;
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
@ConditionalOnMissingBean
public class FreemarkerGuiTemplateResolver implements GuiTemplateResolver {

    private final Configuration freemarkerConfiguration;
    private final TubingPlugin tubingPlugin;
    private final TemplateConfigResolver templateConfigResolver;
    private final DefaultObjectWrapper defaultObjectWrapper;
    private final TubingPermissionService tubingPermissionService;

    public FreemarkerGuiTemplateResolver(@InjectTubingPlugin TubingPlugin tubingPlugin,
                                         TemplateConfigResolver templateConfigResolver,
                                         TubingPermissionService tubingPermissionService) {
        this.tubingPlugin = tubingPlugin;
        this.templateConfigResolver = templateConfigResolver;
        this.tubingPermissionService = tubingPermissionService;
        freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_28);
        defaultObjectWrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_28);
        freemarkerConfiguration.setClassForTemplateLoading(tubingPlugin.getClass(), "/");
    }

    public String resolve(Player player, String templatePath, Map<String, Object> params) {
        try {
            Template template = freemarkerConfiguration.getTemplate(templatePath);
            TemplateModel statics = defaultObjectWrapper.getStaticModels();
            StringWriter stringWriter = new StringWriter();

            params.put("statics", statics);
            params.put("$config", templateConfigResolver);
            params.put("$permissions", tubingPermissionService);
            template.process(params, stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            throw new TubingGuiException("Could not be.garagepoort.mcioc.tubingvelocity.load template: [" + templatePath + "]", e);
        }
    }
}
