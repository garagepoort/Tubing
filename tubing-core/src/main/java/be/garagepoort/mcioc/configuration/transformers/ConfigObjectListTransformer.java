package be.garagepoort.mcioc.configuration.transformers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigObjectListTransformer {

    public static <T> List<T> transform(Class<T> objectClass, List<LinkedHashMap<String, Object>> listOfMaps) {
        return Objects.requireNonNull(listOfMaps).stream().map(map -> ConfigEmbeddedListObjectTransformer.transform(objectClass, map)).collect(Collectors.toList());
    }
}
