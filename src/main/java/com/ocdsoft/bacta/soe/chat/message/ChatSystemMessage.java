package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.util.SOECRC32;

import java.nio.ByteBuffer;

/**
 * Created by crush on 1/12/2015.
 *
 struct __cppobj ChatSystemMessage : GameNetworkMessage
 {
     Archive::AutoVariable<unsigned char> flags;
     Archive::AutoVariable<UnicodeString > message;
     Archive::AutoVariable<UnicodeString > outOfBand;
 }; 
 */
// TODO: Finish this
public class ChatSystemMessage extends GameNetworkMessage {

    private static final short priority = 0x4;
    private static final int messageType = SOECRC32.hashCode(ChatSystemMessage.class.getSimpleName());

    private byte flags;
    private String message; //Unicode
    private String outOfBand; //Unicode

    public ChatSystemMessage(String message) {
        super(priority, messageType);

        this.message = message;
    }

//    public ChatSystemMessage(OutOfBand oob) {
//        super(priority, messageType);
//
//        writeByte(0); //displayType
//        writeInt(0);
//        writeInt(0); //TODO:fix
//        //oob.writeToBuffer(this);
//    }
//
//    public ChatSystemMessage(String message, OutOfBand oob) {
//        super(priority, messageType);
//
//        writeByte(0); //displayType
//        writeUnicode(message);
//
//        writeInt(0); //TODO:Fix
//        //oob.writeToBuffer(this);
//    }
//
//    public ChatSystemMessage(byte flags, String message, ProsePackage pp) {
//        super(priority, messageType);
//        
//        writeByte(flags);
//        writeUnicode(message);
//        writeUnicode(OutOfBandPackager.pack(pp, -1));
//    }

    public ChatSystemMessage(final ByteBuffer buffer) {
        super(priority, messageType);

        buffer.get();
        message = BufferUtil.getUnicode(buffer);
        buffer.getInt();
    }

    @Override
    public void writeToBuffer(final ByteBuffer buffer) {
        buffer.put((byte)0);
        BufferUtil.putUnicode(buffer, message);
        buffer.putInt(0);
    }
}
