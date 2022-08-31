package be.garagepoort.mcioc.tubinggui.exceptions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GuiExceptionHandlerProvider {

    String conditionalOnProperty() default "";

    boolean priority() default false;

    Class multiproviderClass() default Object.class;

    Class[] exceptions();
}
