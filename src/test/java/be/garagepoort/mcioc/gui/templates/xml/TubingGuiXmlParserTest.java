package be.garagepoort.mcioc.gui.templates.xml;

import be.garagepoort.mcioc.gui.model.TubingGui;
import be.garagepoort.mcioc.permissions.TubingPermissionService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TubingGuiXmlParserTest {

    @InjectMocks
    private TubingXmlToTubingGuiMapper tubingGuiXmlParser;

    @Mock
    private TubingPermissionService tubingPermissionService;
    @Mock
    private Player player;
    @Mock
    private Inventory inventory;
    @Mock
    private ItemFactory itemFactory;
    @Mock
    private ItemMeta skullItemMeta;
    @Mock
    private ItemMeta paperItemMeta;
    @Captor
    private ArgumentCaptor<List<String>> loreCaptor;

    @Test
    public void parse() throws IOException {
        when(itemFactory.getItemMeta(Material.SKULL)).thenReturn(skullItemMeta);
        when(itemFactory.getItemMeta(Material.PAPER)).thenReturn(paperItemMeta);

        try (MockedStatic<Bukkit> mocked = mockStatic(Bukkit.class)) {
            mocked.when(Bukkit::getItemFactory).thenReturn(itemFactory);

            mocked.when(() -> Bukkit.createInventory(null, 54, "§bActive mutes")).thenReturn(inventory);
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("tubinggui.xml").getFile());
            String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
            TubingGui tubingGui = tubingGuiXmlParser.toTubingGui(player, content);

            verify(skullItemMeta).setLore(loreCaptor.capture());
            for (String s : loreCaptor.getValue()) {
                System.out.println(s);
            }
        }
    }
}
