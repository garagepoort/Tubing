package be.garagepoort.mcioc.configuration.transformers;

import be.garagepoort.mcioc.configuration.IConfigTransformer;

public class ToEnum<T extends Enum<T>> implements IConfigTransformer<Enum<T>, String> {

    private final Class<T> type;

    public ToEnum(Class<T> type) {
        this.type = type;
    }

    @Override
    public Enum<T> mapConfig(String config) {
        return Enum.valueOf(type, config);
    }
}
