package com.ocdsoft.bacta.soe.message;

public final class NetStatusServerResponse extends SoeMessage {

	public NetStatusServerResponse(short value) {
		super(0x8);
		
		writeShort(value);
		writeInt(0x0); // ?
		writeLong(0); // Client Sent ?
		writeLong(0); // Client Received ?
		writeLong(0);
		writeLong(0);
	}
}
