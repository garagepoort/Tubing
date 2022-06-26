package be.garagepoort.mcioc.tubingbukkit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IocBukkitListener {
    String conditionalOnProperty() default "";

    boolean priority() default false;
}
