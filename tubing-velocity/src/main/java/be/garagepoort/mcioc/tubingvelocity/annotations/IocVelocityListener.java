package be.garagepoort.mcioc.tubingvelocity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IocVelocityListener {

    Class multiproviderClass() default Object.class;

    String conditionalOnProperty() default "";

    boolean priority() default false;
}
