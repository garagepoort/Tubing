package be.garagepoort.mcioc.tubingbukkit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IocBukkitCommandHandler {

    String value();

    String permission() default "";

    boolean onlyPlayers() default false;

    String conditionalOnProperty() default "";

    Class multiproviderClass() default Object.class;

    boolean priority() default false;
}
