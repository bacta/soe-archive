package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.object.chat.ChatAvatarId;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

public class ChatFriendsListUpdate extends GameNetworkMessage {

    private ChatAvatarId avatarId;
    private boolean online;

    public ChatFriendsListUpdate() {
        this.avatarId = new ChatAvatarId("");
        this.online = online;
    }

    public ChatFriendsListUpdate(ChatAvatarId avatarId, boolean online) {
        super(0x03, 0x6CD2FCD8);

        this.avatarId = avatarId;
        this.online = online;
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        avatarId.readFromBuffer(buffer);
        online = BufferUtil.getBoolean(buffer);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        //ChatAvatarId characterName
        //bool isOnline

        avatarId.writeToBuffer(buffer);
        BufferUtil.putBoolean(buffer, online);
    }
}
