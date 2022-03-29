package be.garagepoort.mcioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IocCommandHandler {

    String value();

    String conditionalOnProperty() default "";

    boolean priority() default false;
}
