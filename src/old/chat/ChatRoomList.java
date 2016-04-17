package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.swg.network.soe.object.chat.ChatRoomData;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.util.Collection;

/**
 * A list of all the available chat channels on the server.
 *
 * @direction s->c
 */
public class ChatRoomList extends GameNetworkMessage {
    //ChatRoomData[] roomData

    public ChatRoomList(Collection<ChatRoomData> rooms) throws Exception {
        super(0x02, 0x70DEB197); //ChatRoomList

        writeInt(rooms.size());

        for (ChatRoomData room : rooms) {
            room.writeToBuffer(this);
        }
    }
}
