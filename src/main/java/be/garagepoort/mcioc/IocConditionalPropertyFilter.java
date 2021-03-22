package be.garagepoort.mcioc;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class IocConditionalPropertyFilter {

    public boolean isValidBean(Class a, FileConfiguration config) {
        IocBean annotation = (IocBean) a.getAnnotation(IocBean.class);
        String conditionalOnProperty1 = annotation.conditionalOnProperty();
        if (!StringUtils.isEmpty(conditionalOnProperty1)) {
            String[] split = conditionalOnProperty1.split("=");
            String key = split[0];
            String value = split[1];
            return Objects.requireNonNull(config.getString(key)).equalsIgnoreCase(value);
        }
        return true;
    }
}
