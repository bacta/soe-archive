package com.ocdsoft.bacta.soe;

import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GameNetworkMessageHandled {
	Class<? extends GameNetworkMessage> message();
    ServerType type();
}