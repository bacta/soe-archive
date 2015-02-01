package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.soe.chat.ChatAvatarId;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.util.SOECRC32;

import java.nio.ByteBuffer;

/**
 * Created by crush on 1/12/2015.
 */
public class ChatAddFriend extends GameNetworkMessage {

    private static final short priority = 0x2; //TODO: Get the right value
    private static final int messageType = SOECRC32.hashCode(ChatAddFriend.class.getSimpleName());


    private ChatAvatarId characterName;
    private int sequence;

    public ChatAddFriend() {
        super(priority, messageType);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {

    }
}
