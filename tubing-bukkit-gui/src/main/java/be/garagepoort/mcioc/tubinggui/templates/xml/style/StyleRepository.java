package be.garagepoort.mcioc.tubinggui.templates.xml.style;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.load.InjectTubingPlugin;
import be.garagepoort.mcioc.tubinggui.exceptions.TubingGuiException;
import be.garagepoort.mcioc.tubinggui.style.StyleConfig;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@IocBean
public class StyleRepository {

    private static final List<String> STYLE_KEYS = Arrays.asList("hidden", "slot", "material", "size", "enchanted", "color");
    private final Map<String, StyleConfig> styleIds = new HashMap<>();
    private final Map<String, StyleConfig> styleClasses = new HashMap<>();

    public StyleRepository(@InjectTubingPlugin TubingPlugin tubingPlugin) {
        String directoryPath = tubingPlugin.getDataFolder() + File.separator + "styles";
        File styleDir = new File(directoryPath);
        if (!styleDir.exists()) {
            tubingPlugin.getLogger().info("No styles directory present");
            return;
        }

        for (File file : Objects.requireNonNull(styleDir.listFiles())) {
            if (file.isDirectory()) {
                continue;
            }
            FileConfiguration fileConfiguration = loadConfiguration(file);
            registerStyleConfig(fileConfiguration, "");
        }
    }

    private void registerStyleConfig(ConfigurationSection fileConfiguration, String parentKey) {
        StyleConfig defaultStyleConfig = mapStyleConfig(fileConfiguration);
        registerStyleConfig(parentKey, "", defaultStyleConfig);

        List<String> validKeys = fileConfiguration.getKeys(false)
                .stream().filter(k -> !STYLE_KEYS.contains(k)).collect(Collectors.toList());
        for (String key : validKeys) {
            registerStyleConfig(fileConfiguration.getConfigurationSection(key), parentKey.isEmpty() ? key : parentKey + "_" + key);
        }
    }

    private void registerStyleConfig(String idPrefix, String key, StyleConfig styleConfig) {
        String fullKey = idPrefix + key;
        String[] endSelector = fullKey.split("_");
        if (fullKey.startsWith("$") || endSelector[endSelector.length - 1].startsWith("$")) {
            styleClasses.put(fullKey, styleConfig);
        } else {
            styleIds.put(fullKey, styleConfig);
        }
    }

    private StyleConfig mapStyleConfig(ConfigurationSection section) {
        return new StyleConfig(
                section.contains("color") ? section.getString("color") : null,
                section.contains("material") ? Material.valueOf(section.getString("material")) : null,
                section.contains("hidden") ? section.getBoolean("hidden") : null,
                section.contains("enchanted") ? section.getBoolean("enchanted") : null,
                section.contains("slot") ? section.getInt("slot") : null,
                section.contains("size") ? section.getInt("size") : null);
    }

    public Optional<StyleConfig> getStyleConfigById(StyleId id) {
        if (id == null) {
            return Optional.empty();
        }

        List<String> matchingClasses = styleClasses.keySet().stream()
                .filter(id::matchesClassSelector)
                .sorted(Comparator.comparingInt(c -> c.split("_").length))
                .collect(Collectors.toList());

        StyleConfig styleConfig = null;
        for (String matchingClass : matchingClasses) {
            if (styleConfig == null) {
                styleConfig = styleClasses.get(matchingClass);
            } else {
                styleConfig = styleConfig.update(styleClasses.get(matchingClass));
            }
        }

        Optional<String> matchingId = styleIds.keySet().stream()
                .filter(id::matchesIdSelector)
                .findFirst();
        if (matchingId.isPresent()) {
            if (styleConfig == null) {
                styleConfig = styleIds.get(matchingId.get());
            } else {
                styleConfig = styleConfig.update(styleIds.get(matchingId.get()));
            }
        }
        return Optional.ofNullable(styleConfig);
    }

    public static FileConfiguration loadConfiguration(File file) {
        Validate.notNull(file, "File cannot be null");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (Exception e) {
            throw new TubingGuiException("Cannot be.garagepoort.mcioc.tubingvelocity.load " + file, e);
        }

        return config;
    }

}
