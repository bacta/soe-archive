package com.ocdsoft.bacta.soe.message;

public final class AckAllMessage extends SoeMessage {

	public AckAllMessage(short sequenceNum) {
		super(UdpPacketType.cUdpPacketAckAll1);

		compressed = false;

		buffer.putShort(sequenceNum);
	}
}
