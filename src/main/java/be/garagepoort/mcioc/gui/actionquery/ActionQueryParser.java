package be.garagepoort.mcioc.gui.actionquery;

import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.gui.CurrentAction;
import be.garagepoort.mcioc.gui.GuiParam;
import be.garagepoort.mcioc.gui.GuiParams;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@IocBean
public class ActionQueryParser {

    public Object[] getMethodParams(Method method, GuiActionQuery actionQuery, Player player) {
        Map<String, String> paramMap = actionQuery.getParams();

        Object[] methodParams = new Object[method.getParameterCount()];
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterTypes.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Optional<Annotation> paramAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(GuiParam.class)).findFirst();
            if (paramAnnotation.isPresent()) {
                GuiParam param = (GuiParam) paramAnnotation.get();
                if (paramMap.containsKey(param.value())) {
                    methodParams[i] = toObject(parameterTypes[i], paramMap.get(param.value()));
                } else if (StringUtils.isNotBlank(param.defaultValue())) {
                    methodParams[i] = toObject(parameterTypes[i], param.defaultValue());
                }
            } else if (parameterTypes[i] == Player.class) {
                methodParams[i] = player;
            } else {
                Optional<Annotation> currentActionAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(CurrentAction.class)).findFirst();
                if (currentActionAnnotation.isPresent()) {
                    methodParams[i] = actionQuery.getFullQuery();
                }
            }
            Optional<Annotation> allParamsAnnotation = Arrays.stream(annotations).filter(a -> a.annotationType().equals(GuiParams.class)).findFirst();
            if (allParamsAnnotation.isPresent()) {
                methodParams[i] = paramMap;
            }
        }
        return methodParams;
    }

    private Object toObject(Class clazz, String value) {
        if (Boolean.class == clazz || Boolean.TYPE == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz || Byte.TYPE == clazz) return Byte.parseByte(value);
        if (Short.class == clazz || Short.TYPE == clazz) return Short.parseShort(value);
        if (Integer.class == clazz || Integer.TYPE == clazz) return Integer.parseInt(value);
        if (Long.class == clazz || Long.TYPE == clazz) return Long.parseLong(value);
        if (Float.class == clazz || Float.TYPE == clazz) return Float.parseFloat(value);
        if (Double.class == clazz || Double.TYPE == clazz) return Double.parseDouble(value);
        return value;
    }
}
