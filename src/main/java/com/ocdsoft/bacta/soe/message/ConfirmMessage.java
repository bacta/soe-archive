package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.engine.utils.BufferUtil;

public final class ConfirmMessage extends SoeMessage {

	public ConfirmMessage(final byte crcLength,
                          final int connectionID,
                          final int sessionKey,
                          final byte cryptMethod,
                          final boolean useComp,
                          final int udpSize) {

		super(UdpPacketType.cUdpPacketConfirm);

		buffer.putInt(connectionID);
        buffer.putInt(sessionKey);
		buffer.put(crcLength);
		BufferUtil.putBoolean(buffer, useComp);
		buffer.put(cryptMethod);
        buffer.putInt(udpSize);
	}
}
