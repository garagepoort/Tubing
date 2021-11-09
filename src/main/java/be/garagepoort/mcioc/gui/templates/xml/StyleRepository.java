package be.garagepoort.mcioc.gui.templates.xml;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.gui.exceptions.TubingGuiException;
import be.garagepoort.mcioc.gui.style.StyleConfig;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@IocBean
public class StyleRepository {

    private final Map<String, StyleConfig> styleIds = new HashMap<>();
    private final Map<String, StyleConfig> styleClasses = new HashMap<>();

    public StyleRepository() {
        String directoryPath = TubingPlugin.getPlugin().getDataFolder() + File.separator + "styles";
        File styleDir = new File(directoryPath);
        if (!styleDir.exists()) {
            TubingPlugin.getPlugin().getLogger().info("No styles directory present");
            return;
        }

        for (File file : Objects.requireNonNull(styleDir.listFiles())) {
            FileConfiguration fileConfiguration = loadConfiguration(file);
            String fileNameWithoutExtension = getFileNameWithoutExtension(file);
            for (String key : fileConfiguration.getKeys(false)) {
                if (key.contains("$")) {
                    styleClasses.put(fileNameWithoutExtension + "_" + key, mapStyleConfig(fileConfiguration.getConfigurationSection(key)));
                } else {
                    styleIds.put(fileNameWithoutExtension + "_" + key, mapStyleConfig(fileConfiguration.getConfigurationSection(key)));
                }
            }
        }
    }

    private StyleConfig mapStyleConfig(ConfigurationSection section) {
        return new StyleConfig(
                section.getString("color"),
                section.contains("material") ? Material.valueOf(section.getString("material")) : null,
                section.getBoolean("hidden"),
                section.getBoolean("enchanted"),
                section.contains("slot") ? section.getInt("slot") : null,
                section.contains("size") ? section.getInt("size") : null);
    }

    private String getFileNameWithoutExtension(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf('.'));
    }

    public Optional<StyleConfig> getStyleConfigById(StyleId id) {
        TubingPlugin.getPlugin().getLogger().info("Registered classes: [" + String.join(",", styleClasses.keySet()) + "]");
        List<String> matchingClasses = styleClasses.keySet().stream()
                .filter(id::matchesClassSelector)
                .sorted(Comparator.comparingInt(c -> c.split("_").length))
                .collect(Collectors.toList());

        StyleConfig styleConfig = null;
        for (String matchingClass : matchingClasses) {
            TubingPlugin.getPlugin().getLogger().info("Matched style: [" + matchingClass + "]");
            if (styleConfig == null) {
                styleConfig = styleClasses.get(matchingClass);
            } else {
                styleConfig = styleConfig.update(styleClasses.get(matchingClass));
            }
        }

        if (id.getFullId() != null && styleIds.containsKey(id.getFullId())) {
            TubingPlugin.getPlugin().getLogger().info("Matched ID: [" + id.getFullId() + "]");
            if (styleConfig == null) {
                styleConfig = styleIds.get(id.getFullId());
            } else {
                styleConfig = styleConfig.update(styleIds.get(id.getFullId()));
            }
        }

        TubingPlugin.getPlugin().getLogger().info("Style found: [" + styleConfig + "]");
        return Optional.ofNullable(styleConfig);
    }

    public static FileConfiguration loadConfiguration(File file) {
        Validate.notNull(file, "File cannot be null");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (Exception e) {
            throw new TubingGuiException("Cannot load " + file, e);
        }

        return config;
    }

}
