package be.garagepoort.mcioc.tubingbukkit.annotations;

import be.garagepoort.mcioc.tubingbukkit.commands.SubCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IocBukkitSubCommand {

    String root();

    String action();

    String conditionalOnProperty() default "";

    boolean priority() default false;

    Class multiproviderClass() default SubCommand.class;
}
