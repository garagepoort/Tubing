package be.garagepoort.mcioc.configuration.transformers;

import be.garagepoort.mcioc.configuration.IConfigTransformer;
import be.garagepoort.mcioc.configuration.files.ConfigurationException;

import java.util.Collection;
import java.util.stream.Collectors;

public class ToLowerCase implements IConfigTransformer<Object, Object> {
    @Override
    public Object mapConfig(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof String) {
            return ((String) input).toLowerCase();
        }
        if (input instanceof Collection) {
            return ((Collection<?>) input).stream().map(s -> ((String) s).toLowerCase()).collect(Collectors.toList());
        }
        throw new ConfigurationException("ToLowerCase transformer need String or Collection as input, but [" + input.getClass() + "] was given");
    }
}
