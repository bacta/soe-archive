package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MessageHandled {
    Class<? extends GameNetworkMessage>[] handles();
    ServerType[] type() default ServerType.GAME;
}