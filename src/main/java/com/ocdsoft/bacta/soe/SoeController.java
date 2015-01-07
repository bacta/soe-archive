package com.ocdsoft.bacta.soe;

import com.ocdsoft.bacta.soe.message.UdpPacketType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SoeController {

	UdpPacketType[] handles();
}
