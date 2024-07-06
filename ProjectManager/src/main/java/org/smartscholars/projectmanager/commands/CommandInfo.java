package org.smartscholars.projectmanager.commands;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {
    String name();
    String description();
    Permission[] permissions() default {};
    CommandOption[] options() default {};
}