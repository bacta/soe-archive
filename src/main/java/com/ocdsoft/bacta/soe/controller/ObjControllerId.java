package com.ocdsoft.bacta.soe.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ObjControllerId {
	int id();
	Class<?> handles();
}
