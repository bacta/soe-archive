package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.object.chat.ChatAvatarId;

import java.nio.ByteBuffer;

public class ChatInstantMessageToClient extends GameNetworkMessage {

    final ChatAvatarId sender;
    final String message;

    public ChatInstantMessageToClient(ChatAvatarId sender, String message) {
        super(0x04, 0x3C565CED);

        this.sender = sender;
        this.message = message;
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {

    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        sender.writeToBuffer(buffer);
        BufferUtil.putUnicode(buffer, message);
        buffer.putInt(0); //StringIdParameter
    }
}
