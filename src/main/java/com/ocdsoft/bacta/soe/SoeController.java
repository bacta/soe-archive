package com.ocdsoft.bacta.soe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SoeController {

	short opcode();

	Class<?> handles();

}
