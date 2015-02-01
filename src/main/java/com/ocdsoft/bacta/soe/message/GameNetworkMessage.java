package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.engine.buffer.ByteBufferWritable;

/**
 * Created by Kyle on 3/26/14.
 */
public abstract class GameNetworkMessage implements ByteBufferWritable {

    private final short priority;
    private final int messageType;

    protected GameNetworkMessage(final short priority, final int messageType) {
        this.priority = priority;
        this.messageType = messageType;
    }

    public short getPriority() {
        return priority;
    }

    public int getMessageType() {
        return messageType;
    }
}
