package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

public class ConnectPlayerMessage extends GameNetworkMessage {
	public ConnectPlayerMessage() {
		super(0x02, 0x6137556F);

		writeInt(0);
	}
}
