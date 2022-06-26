package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.IocException;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.load.InjectTubingPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TubingPluginInjector {
    public static void inject(Object bean, TubingPlugin tubingPlugin) {
        setTubingPlugin(tubingPlugin, bean);
    }

    private static void setTubingPlugin(TubingPlugin tubingPlugin, Object o) {
        try {
            for (Field f : getAllFields(new LinkedList<>(), o.getClass())) {
                if (!f.isAnnotationPresent(InjectTubingPlugin.class)) {
                    continue;
                }
                f.setAccessible(true);
                f.set(o, tubingPlugin);
            }
        } catch (IllegalAccessException e) {
            throw new IocException("Cannot inject TubingPlugin. Make sure the field is public", e);
        }
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
