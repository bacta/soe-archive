package com.ocdsoft.bacta.soe.message;

public final class OutOfOrderMessage extends SoeMessage {

	public OutOfOrderMessage(short sequenceNum) {
		super(0x11);

		compressed = false;

		writeShortBE(sequenceNum);
	}
}
