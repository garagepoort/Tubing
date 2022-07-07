package be.garagepoort.mcioc.tubingbukkit.config.transformers;

import be.garagepoort.mcioc.configuration.IConfigTransformer;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;
import org.bukkit.Material;

import java.util.Collection;
import java.util.stream.Collectors;

public class ToMaterial implements IConfigTransformer<Object, Object> {
    @Override
    public Object mapConfig(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof String) {
            return Material.valueOf(((String) input).toUpperCase());
        }
        if (input instanceof Collection) {
            return ((Collection<?>) input).stream()
                .map(s -> Material.valueOf(((String) s).toUpperCase()))
                .collect(Collectors.toList());
        }
        throw new ConfigurationException("ToMaterial transformer needs String or Collection as input, but [" + input.getClass() + "] was given");
    }
}
