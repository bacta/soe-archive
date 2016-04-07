package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.util.SOECRC32;

import java.nio.ByteBuffer;

/**
 * Created by crush on 1/12/2015.
 * 
 struct __cppobj ChatOnConnectAvatar : GameNetworkMessage
 {
 };
 */
public class ChatOnConnectAvatar extends GameNetworkMessage{

    private static final short priority = 0x1;
    private static final int messageType = SOECRC32.hashCode(ChatOnConnectAvatar.class.getSimpleName());

    public ChatOnConnectAvatar() {
        super(priority, messageType);
    }

    public ChatOnConnectAvatar(final ByteBuffer buffer) {
        super(priority, messageType);
    }

    @Override
    public void writeToBuffer(final ByteBuffer buffer) {
        
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {

    }
}
