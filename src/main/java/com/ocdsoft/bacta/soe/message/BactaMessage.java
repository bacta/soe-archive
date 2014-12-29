package com.ocdsoft.bacta.soe.message;

public class BactaMessage extends SoeMessage {

	public BactaMessage() {
		super(0x4);

		compressed = false;
        process = false;
	}
}
