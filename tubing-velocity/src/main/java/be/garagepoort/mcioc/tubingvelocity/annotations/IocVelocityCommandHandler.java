package be.garagepoort.mcioc.tubingvelocity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IocVelocityCommandHandler {

    String conditionalOnProperty() default "";

    Class multiproviderClass() default Object.class;

    boolean priority() default false;

    String value();

    String[] aliases() default "";
}
