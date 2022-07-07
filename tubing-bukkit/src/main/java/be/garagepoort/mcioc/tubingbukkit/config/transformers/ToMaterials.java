package be.garagepoort.mcioc.tubingbukkit.config.transformers;

import be.garagepoort.mcioc.configuration.IConfigTransformer;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ToMaterials implements IConfigTransformer<Object, Object> {
    @Override
    public Object mapConfig(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof String) {
            return getMaterials((String) input);
        }
        if (input instanceof Collection) {
            return ((Collection<?>) input).stream()
                .flatMap(s -> getMaterials((String) s).stream())
                .collect(Collectors.toSet());
        }
        throw new ConfigurationException("ToMaterial transformer needs String or Collection as input, but [" + input.getClass() + "] was given");
    }

    public Set<Material> getMaterials(String input) {
        String regex = ("\\Q" + input.toUpperCase() + "\\E").replace("*", "\\E.*\\Q");
        return Arrays.stream(Material.values())
            .filter(m -> m.name().matches(regex))
            .collect(Collectors.toSet());
    }
}
