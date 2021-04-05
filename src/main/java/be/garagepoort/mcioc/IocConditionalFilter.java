package be.garagepoort.mcioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static be.garagepoort.mcioc.ReflectionUtils.getMethodsAnnotatedWith;

public class IocConditionalFilter {

    public boolean isValidBean(Class a) {
        List<Method> methodsAnnotatedWith = getMethodsAnnotatedWith(a, IocConditional.class);
        for (Method method : methodsAnnotatedWith) {
            try {
                boolean valid = (boolean) method.invoke(null);
                if(!valid) {
                    return false;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IocException("Could not validate @IocConditional method. Make sure this method is static and returns a boolean");
            }
        }
        return true;
    }
}
