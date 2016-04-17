package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

public class ChatOnAddFriend extends GameNetworkMessage {


	public ChatOnAddFriend() {
		super(0x03, 0x2B2A0D94);

	}

	@Override
	public void readFromBuffer(ByteBuffer buffer) {

	}

	@Override
	public void writeToBuffer(ByteBuffer buffer) {

    }
}
