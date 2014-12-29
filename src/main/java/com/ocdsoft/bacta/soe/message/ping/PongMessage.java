package com.ocdsoft.bacta.soe.message.ping;


import com.ocdsoft.bacta.soe.message.SoeMessage;

public class PongMessage extends SoeMessage {

	public PongMessage(short readShort, byte readByte) {
		super(0x6);
		writeShort(readShort);
		writeByte(readByte);
		writeInt(0);
		writeInt(0);
		writeInt(0);
		writeShort(0);
    }

    public PongMessage(int someInt) {

        writeIntBE(someInt);
//        writeInt(0);
//        writeInt(0);
//        writeInt(0);
//        writeShort(0);
    }
	
}
