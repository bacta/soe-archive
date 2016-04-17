package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

public class ChatOnChangeIgnoreStatus extends GameNetworkMessage {
	
	public ChatOnChangeIgnoreStatus() {
		super(0x06, 0x70E9DA0F);
		
		//objectid - whos objectid? the list owner or the person being changed?
		//ChatAvatarId
		//Unknown Int
		//Byte add?
		//Unknown Int
	}
	
}
