package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

/**
 * A request for the server to send the client a list of all available chat channels.
 * @direction c->s
 */
public class ChatRequestRoomList extends GameNetworkMessage {
	public ChatRequestRoomList() {
		super(0x01, 0x4C3D2CFA);  //ChatRequestRoomList
	}

	@Override
	public void readFromBuffer(ByteBuffer buffer) {

	}

	@Override
	public void writeToBuffer(ByteBuffer buffer) {

	}
}
