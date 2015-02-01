package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.soe.chat.ChatAvatarId;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

/**
 * Created by crush on 1/12/2015.
 */
public class ChatAddFriend extends GameNetworkMessage {
    private ChatAvatarId characterName;
    private int sequence;

    public ChatAddFriend(int priority, int messageType) {
        super(priority, messageType);
    }

    @Override
    public ByteBuffer toBuffer() {
        return null;
    }
}
