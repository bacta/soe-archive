package com.ocdsoft.bacta.soe.serialize;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kyle on 5/4/2016.
 */
public class GameNetworkMessageSerializerImpl implements GameNetworkMessageSerializer {
    @Override
    public <T extends GameNetworkMessage> ByteBuffer writeToBuffer(T message) {

        // TODO: Better buffer creation
        ByteBuffer buffer = ByteBuffer.allocate(1500).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putShort(message.getPriority());
        buffer.putInt(message.getMessageType());

        message.writeToBuffer(buffer);
        buffer.limit(buffer.position());
        buffer.rewind();

        return buffer;
    }

    @Override
    public <T extends GameNetworkMessage> T readFromBuffer(ByteBuffer buffer) {
        return null;
    }
}
