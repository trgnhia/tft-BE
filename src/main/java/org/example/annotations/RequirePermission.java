package org.example.annotations;

import org.example.common.enums.PERMISSION;
import org.example.common.enums.RESOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequirePermission {
    RESOURCE resource();

    PERMISSION permission() default PERMISSION.READ;
}
