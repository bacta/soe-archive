package com.ocdsoft.bacta.soe.object.chat;

import com.ocdsoft.bacta.engine.buffer.ByteBufferSerializable;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.List;

public class ChatRoomData implements ByteBufferSerializable {
    private String serverName;
    private String serviceName;

    @Getter
    @Setter
    private int roomId;
    @Getter
    @Setter
    private boolean moderated;
    @Getter
    @Setter
    private boolean membersOnly;
    @Getter
    @Setter
    private boolean nonanonymous;
    @Getter
    @Setter
    private boolean passwordProtected;
    @Getter
    @Setter
    private boolean persistent;
    @Getter
    @Setter
    private String address;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private ChatAvatarId owner;
    @Getter
    @Setter
    private ChatAvatarId creator;
    @Getter
    @Setter
    private List<ChatAvatarId> moderators;
    @Getter
    @Setter
    private List<ChatAvatarId> users;

    public ChatRoomData(String address, ChatAvatarId owner) {
        this.address = address;
        this.owner = owner;
        this.creator = owner;
        this.serverName = owner.getGameCode();
        this.serviceName = owner.getCluster();
    }

    public ChatRoomData(ByteBuffer buffer) {
        roomId = buffer.getInt();
        passwordProtected = BufferUtil.getBoolean(buffer);
        passwordProtected = BufferUtil.getBoolean(buffer);
        nonanonymous = BufferUtil.getBoolean(buffer);
        persistent = BufferUtil.getBoolean(buffer);
        membersOnly = BufferUtil.getBoolean(buffer);
        moderated = BufferUtil.getBoolean(buffer);

        address = BufferUtil.getAscii(buffer);

        owner = new ChatAvatarId(buffer);
        creator = new ChatAvatarId(buffer);

        title = BufferUtil.getUnicode(buffer);

        buffer.getInt(); //Moderator list
        buffer.getInt(); //User list
    }

    public String getFullAddress() {
        return address + "@" + serviceName + "." + serverName;
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(roomId);
        BufferUtil.putBoolean(buffer, passwordProtected);
        BufferUtil.putBoolean(buffer, passwordProtected);
        BufferUtil.putBoolean(buffer, nonanonymous);
        BufferUtil.putBoolean(buffer, persistent);
        BufferUtil.putBoolean(buffer, membersOnly);
        BufferUtil.putBoolean(buffer, moderated);
        BufferUtil.putAscii(buffer, address);

        owner.writeToBuffer(buffer);
        creator.writeToBuffer(buffer);

        BufferUtil.putUnicode(buffer, title);

        buffer.putInt(0); //Moderator list
        buffer.putInt(0); //User list
    }
}
