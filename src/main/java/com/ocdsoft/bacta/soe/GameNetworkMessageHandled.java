package com.ocdsoft.bacta.soe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GameNetworkMessageHandled {
	Class<?> value();
}