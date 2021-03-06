package com.ericlam.mc.eld.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用於標註指令
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Commander {

    String name();

    String description();

    boolean playerOnly() default false;

    String permission() default "";

    String[] alias() default {};

}
