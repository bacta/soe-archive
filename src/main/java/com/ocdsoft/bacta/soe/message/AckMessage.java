package com.ocdsoft.bacta.soe.message;

public final class AckMessage extends SoeMessage {

	public AckMessage(short sequenceNum) {
		super(UdpPacketType.cUdpPacketAckAll1);

		compressed = false;

		buffer.putShort(sequenceNum);
	}
}
