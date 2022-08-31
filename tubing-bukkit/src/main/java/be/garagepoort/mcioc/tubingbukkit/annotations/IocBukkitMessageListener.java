package be.garagepoort.mcioc.tubingbukkit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IocBukkitMessageListener {
    String channel();

    String conditionalOnProperty() default "";

    boolean priority() default false;

    Class multiproviderClass() default Object.class;
}
