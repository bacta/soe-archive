package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

public class ChatOnSendInstantMessage extends GameNetworkMessage {

    private int errorCode;
    private int sequence;

    public ChatOnSendInstantMessage(int sequence, int errorCode) {
        super(0x03, 0x88DBB381);

        this.errorCode = errorCode;
        this.sequence = sequence;
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        errorCode = buffer.getInt();
        sequence = buffer.getInt();
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(errorCode);
        buffer.putInt(sequence);
    }
}
