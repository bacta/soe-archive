package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.engine.utils.BufferUtil;

public final class SessionResponse extends SoeMessage {

	public SessionResponse(final int crcLength,
                           final int connectionID,
                           final int sessionKey,
                           final byte cryptMethod,
                           final boolean useComp,
                           final int udpSize) {

		super(UdpPacketType.cUdpPacketConfirm);

		process = false;

		buffer.putInt(connectionID);
        buffer.putInt(sessionKey);
		buffer.put((byte) crcLength);
		BufferUtil.putBoolean(buffer, useComp);
		buffer.put(cryptMethod);
        buffer.putInt(udpSize);
	}
}
