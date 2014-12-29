package com.ocdsoft.bacta.soe.message;

public final class SessionRequest extends SoeMessage {

	public SessionRequest() {
		super(0x1);
		
		process = false;
		
		writeInt(2); //crclength
		writeInt(1000); //connection id
		writeInt(496); //client udp size
	}
	
}
