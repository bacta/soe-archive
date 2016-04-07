package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.engine.buffer.ByteBufferSerializable;

import java.nio.ByteBuffer;

/**
 * Created by Kyle on 3/26/14.
 */
public abstract class GameNetworkMessage implements ByteBufferSerializable {

    private final short priority;
    private final int messageType;

    protected GameNetworkMessage(final int priority, final int messageType) {
        this.priority = (short) priority;
        this.messageType = messageType;
    }

    public short getPriority() {
        return priority;
    }

    public int getMessageType() {
        return messageType;
    }
}
