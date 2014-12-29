package com.ocdsoft.bacta.soe.message;

public final class AckMessage extends SoeMessage {

	public AckMessage(short sequenceNum) {
		super(0x15);

		compressed = false;

		writeShortBE(sequenceNum);
	}
}
