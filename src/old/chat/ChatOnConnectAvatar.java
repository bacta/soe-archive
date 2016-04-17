package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

public class ChatOnConnectAvatar extends GameNetworkMessage {
    public ChatOnConnectAvatar() {
        super(0x01, 0xD72FE9BE);  //ChatOnConnectAvatar

        //empty struct. just tells the client the avatar connected to the chat server.
    }

    @Override
    public ByteBuffer toBuffer() {
        return null;
    }
}
