package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.object.chat.ChatAvatarId;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

public class ChatAddFriend extends GameNetworkMessage {

    private ChatAvatarId avatarId;
    private String friendName;

    public ChatAddFriend(ChatAvatarId avatarId, String friendName) {
        super(0x04, 0x6FE7BD90);

        this.avatarId = avatarId;
        this.friendName = friendName;
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        avatarId.readFromBuffer(buffer);
        friendName = BufferUtil.getAscii(buffer);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        avatarId.writeToBuffer(buffer);
        BufferUtil.putAscii(buffer, friendName);
        buffer.putInt(1); //unk

        //ChatAvatarId characterName;
        //int sequence
    }
}
