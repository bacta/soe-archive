package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MessageHandled {
    String command() default "";
    int id() default -1;
    Class<?> handles() default Object.class;
    ServerType[] type() default ServerType.GAME;
}