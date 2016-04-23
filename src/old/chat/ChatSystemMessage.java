package com.ocdsoft.bacta.swg.precu.message.chat;


import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.swg.precu.message.game.outofband.OutOfBand;
import com.ocdsoft.bacta.swg.precu.message.game.outofband.OutOfBandPackager;
import com.ocdsoft.bacta.swg.precu.message.game.outofband.ProsePackage;

public class ChatSystemMessage extends GameNetworkMessage {
	//0x04
	//0x6D2A6413 "ChatSystemMessage"
	
	//display type: 0 = show message in chat window and on screen
	//display type: 2 = show message in chat window only


    //byte flags
    //UnicodeString message
    //UnicodeString outOfBand
	
	public ChatSystemMessage(String message) {
		super(0x04, 0x6D2A6413);
		
		writeByte(0);
		writeUnicode(message);
		writeInt(0);
	}
	
	public ChatSystemMessage(OutOfBand oob) {
		super(0x04, 0x6D2A6413);
		
		writeByte(0); //displayType
		writeInt(0);
        writeInt(0); //TODO:fix
		//oob.writeToBuffer(this);
	}

	public ChatSystemMessage(String message, OutOfBand oob) {
		super(0x04, 0x6D2A6413);
		
		writeByte(0); //displayType
		writeUnicode(message);

        writeInt(0); //TODO:Fix
		//oob.writeToBuffer(this);
	}

    public ChatSystemMessage(byte flags, String message, ProsePackage pp) {
        super(0x04, 0x6D2A6413);
        writeByte(flags);
        writeUnicode(message);
        writeUnicode(OutOfBandPackager.pack(pp, -1));
    }
}