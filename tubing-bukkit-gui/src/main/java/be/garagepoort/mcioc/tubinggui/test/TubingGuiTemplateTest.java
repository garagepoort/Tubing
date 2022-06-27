package be.garagepoort.mcioc.tubinggui.test;

import be.garagepoort.mcioc.IocContainer;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.configuration.ConfigurationLoader;
import be.garagepoort.mcioc.configuration.files.ConfigurationFile;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.YamlConfiguration;
import be.garagepoort.mcioc.tubingbukkit.permissions.TubingPermissionService;
import be.garagepoort.mcioc.tubinggui.GuiActionService;
import be.garagepoort.mcioc.tubinggui.actionquery.ActionQueryParser;
import be.garagepoort.mcioc.tubinggui.exceptions.TubingGuiException;
import be.garagepoort.mcioc.tubinggui.history.GuiHistoryStack;
import be.garagepoort.mcioc.tubinggui.model.InventoryMapper;
import be.garagepoort.mcioc.tubinggui.model.TubingGui;
import be.garagepoort.mcioc.tubinggui.style.TubingGuiStyleIdViewProvider;
import be.garagepoort.mcioc.tubinggui.templates.ChatTemplateResolver;
import be.garagepoort.mcioc.tubinggui.templates.FreemarkerGuiTemplateResolver;
import be.garagepoort.mcioc.tubinggui.templates.GuiTemplateProcessor;
import be.garagepoort.mcioc.tubinggui.templates.GuiTemplateResolver;
import be.garagepoort.mcioc.tubinggui.templates.TemplateConfigResolver;
import be.garagepoort.mcioc.tubinggui.templates.TubingXmlConfigParser;
import be.garagepoort.mcioc.tubinggui.templates.xml.TubingXmlToTubingGuiMapper;
import be.garagepoort.mcioc.tubinggui.templates.xml.style.TubingGuiStyleParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class TubingGuiTemplateTest {
    protected static final UUID PLAYER_UUID = UUID.fromString("3723136f-40dd-4f27-8505-8fa880f14e95");
    protected GuiActionService guiActionService;

    @Mock
    protected Player player;
    @Mock
    protected ConfigurationLoader configurationLoader;
    @Mock
    protected ChatTemplateResolver chatTemplateResolver;
    @Mock
    protected TubingXmlToTubingGuiMapper tubingGuiXmlParser;
    @Mock
    protected TubingGuiStyleParser tubingGuiStyleParser;
    @Mock
    protected InventoryMapper inventoryMapper;
    @Mock
    protected TubingGuiStyleIdViewProvider tubingGuiStyleIdViewProvider;
    @Mock
    protected IocContainer iocContainer;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected TubingPlugin tubingPlugin;

    protected TemplateConfigResolver templateConfigResolverSpy;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TubingGui tubingGui;

    @Captor
    private ArgumentCaptor<String> xmlCaptor;

    @BeforeEach
    public void setUp() {
        TubingPlugin pluginMock = Mockito.mock(getPluginClass());
        when(pluginMock.getName()).thenReturn("staffplus");
        when(pluginMock.getIocContainer()).thenReturn(iocContainer);


        Map<String, FileConfiguration> collect = getConfigurationFiles().stream()
            .collect(Collectors.toMap(ConfigurationFile::getIdentifier, c -> loadConfig(c.getPath()), (a, b) -> a));
        when(configurationLoader.getConfigurationFiles()).thenReturn(collect);

        Object guiController = getGuiController();
        doReturn(guiController).when(iocContainer).get(guiController.getClass());

        when(tubingGuiXmlParser.toTubingGui(ArgumentMatchers.eq(player), ArgumentMatchers.any())).thenReturn(tubingGui);
        when(player.getUniqueId()).thenReturn(PLAYER_UUID);

        TemplateConfigResolver templateConfigResolver = new TemplateConfigResolver(configurationLoader);
        templateConfigResolverSpy = Mockito.spy(templateConfigResolver);

        GuiTemplateResolver guiTemplateResolver = new FreemarkerGuiTemplateResolver(pluginMock, templateConfigResolverSpy, getTubingPermissionService());
        GuiTemplateProcessor guiTemplateProcessor = new GuiTemplateProcessor(guiTemplateResolver, new TubingXmlConfigParser(templateConfigResolverSpy), tubingGuiXmlParser, tubingGuiStyleParser);

        guiActionService = new GuiActionService(
            pluginMock,
            configurationLoader,
            guiTemplateProcessor,
            chatTemplateResolver,
            new ActionQueryParser(),
            new TubingBukkitUtilStub(),
            inventoryMapper,
            tubingGuiStyleIdViewProvider,
            new GuiHistoryStack(),
            Collections.emptyList());

        guiActionService.loadGuiController(guiController.getClass());
    }

    public void validateSnapshot(Player player, String actionQuery, String snapshotFile) throws URISyntaxException, IOException {
        guiActionService.executeAction(player, actionQuery);

        verify(tubingGuiXmlParser).toTubingGui(eq(player), xmlCaptor.capture());
        validateMaterials(xmlCaptor.getValue());
        validateXml(xmlCaptor.getValue(), snapshotFile);
    }

    public abstract Object getGuiController();

    public abstract Class<? extends TubingPlugin> getPluginClass();

    public abstract List<ConfigurationFile> getConfigurationFiles();

    public abstract TubingPermissionService getTubingPermissionService();

    public void validateMaterials(String guiXml) {
        Document document = Jsoup.parse(guiXml);
        Elements materialElements = document.getElementsByAttribute("material");
        for (Element materialElement : materialElements) {
            String materialString = materialElement.attr("material");
            Material.valueOf(materialString);
        }
    }

    public void validateXml(String actualXml, String pathToXml) throws URISyntaxException, IOException {
        java.net.URL url = TubingGuiTemplateTest.class.getResource(pathToXml);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        String templateXml = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
        Assertions.assertEquals(trim(templateXml), trim(actualXml));
    }

    public FileConfiguration loadConfig(String path) {
        InputStream resource = getPluginClass().getResourceAsStream("/" + path);

        Validate.notNull(resource, "File cannot be null");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(IOUtils.toString(resource));
        } catch (Exception e) {
            throw new TubingGuiException("Cannot be.garagepoort.mcioc.tubingvelocity.load " + path, e);
        }

        return config;
    }

    public static String trim(String input) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(input));) {
            String line;
            while ((line = reader.readLine()) != null)
                result.append(line.trim());
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
