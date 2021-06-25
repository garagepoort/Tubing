package be.garagepoort.mcioc.configuration;

public interface IConfigTransformer<T, S> {

    T mapConfig(S config);
}
