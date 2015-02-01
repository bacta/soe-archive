package com.ocdsoft.bacta.soe.annotation;

import com.ocdsoft.bacta.soe.connection.ConnectionRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kburkhardt on 1/31/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RolesAllowed {
    ConnectionRole[] value() default {ConnectionRole.AUTHENTICATED};
}
