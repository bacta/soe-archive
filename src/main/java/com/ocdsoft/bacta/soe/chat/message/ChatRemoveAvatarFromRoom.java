package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.soe.chat.ChatAvatarId;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

/**
 * Created by crush on 1/12/2015.
 */
public class ChatRemoveAvatarFromRoom extends GameNetworkMessage {
    private ChatAvatarId avatarId;
    private String roomName;

    public ChatRemoveAvatarFromRoom(int priority, int messageType) {
        super(priority, messageType);
    }

    @Override
    public ByteBuffer toBuffer() {
        return null;
    }
}
