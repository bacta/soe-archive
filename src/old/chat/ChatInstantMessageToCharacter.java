package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

public class ChatInstantMessageToCharacter extends GameNetworkMessage {

    public ChatInstantMessageToCharacter(int priority, int messageType) {
        super(priority, messageType);
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {

    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {

    }
    //ChatAvatarId characterName
    //UnicodeString message
    //UnicodeString outOfBand
    //int sequence
}
