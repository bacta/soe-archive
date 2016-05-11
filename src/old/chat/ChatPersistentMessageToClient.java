package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.object.chat.ChatAvatarId;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.swg.precu.shared.util.SOECRC32;

public class ChatPersistentMessageToClient extends GameNetworkMessage {

    //ChatPersistentMessageToClientData data;


    public ChatPersistentMessageToClient(ChatAvatarId avatarId, String subject) {
        super(0x02, 0x08485E17);

        writeAscii(avatarId.getName());
        writeAscii("");
        writeAscii(""); //Are these two ascii strings, or is it actually an OOB?
        writeInt(1254);

        writeByte(1); //send body

        writeUnicode("");

        writeUnicode(subject);

        writeInt(0);

        writeByte('U');
        writeInt(0);
    }

    public ChatPersistentMessageToClient(int i) {
        super(0x02, 0x08485E17);

        writeAscii("crush");
        writeAscii("");
        writeAscii(""); //Are these two ascii strings, or is it actually an OOB?
        writeInt(1254);

        writeByte(0); //send body

        writeUnicode("Test body");

        writeUnicode("Test subject");

        //parameters
        writeInt(0x1D);
        writeShort(0x01);
        writeByte(0x04); //WaypointDataBase
        writeInt(i);
        writeInt(0);
        writeFloat(0);
        writeFloat(0);
        writeFloat(0);
        writeLong(0);
        writeInt(SOECRC32.hashCode("tatooine"));
        writeUnicode("Test");
        writeLong(42359);
        writeByte(1);
        writeByte(1);
        writeByte(0); //Add a filler

        writeByte('U');
        writeInt(0);
    }
}
