package be.garagepoort.mcioc.configuration;

import java.util.LinkedHashMap;
import java.util.List;

public interface IConfigListTransformer {

    List mapConfig(List<LinkedHashMap<String, Object>> configList);
}
