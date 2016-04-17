package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

public class ChatOnChangeFriendStatus extends GameNetworkMessage {

    public ChatOnChangeFriendStatus(long objectId) {
        super(0x06, 0x54336726);

        writeLong(objectId);
        writeAscii("swg");
        writeAscii("bacta");
        writeAscii("crush");
        writeInt(0);
        writeInt(1);
        writeByte(0);

        //objectid - whos objectid? the list owner or the person being changed?
        //ChatAvatarId
        //Unknown Int
        //Add byte/boolean?
        //Unknown Int? Seen 0 usually, but 26 in ANH packet for already existing friend add request
    }

}
