package com.ocdsoft.bacta.soe.message;

public final class SessionResponse extends SoeMessage {

	public SessionResponse(int crcLength, int connectionID, int sessionKey, byte cryptMethod, boolean useComp, int udpSize) {
		super(0x2);

		process = false;

		writeIntBE(connectionID);
		writeIntBE(sessionKey);
		writeByte(crcLength);
		writeBoolean(useComp);
		writeByte(cryptMethod);
		writeIntBE(udpSize);
	}
}
