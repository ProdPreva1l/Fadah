package info.preva1l.fadah.utils.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandArgs {
    String name();
    String permission() default "";
    String[] aliases() default {};
    boolean inGameOnly() default true;
    boolean async() default false;
}