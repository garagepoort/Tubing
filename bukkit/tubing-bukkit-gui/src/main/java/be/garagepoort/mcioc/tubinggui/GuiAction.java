package be.garagepoort.mcioc.tubinggui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GuiAction {

    String value();

    boolean overrideHistory() default true;

    boolean skipHistory() default false;
}
