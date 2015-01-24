package com.ocdsoft.bacta.soe.message;

import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * Created by Kyle on 3/26/14.
 */
public abstract class GameNetworkMessage {

    @Getter
    protected short priority;

    @Getter
    protected int messageType;

    public void serialize(ByteBuffer buffer) {
        buffer.putShort(priority);
        buffer.putInt(messageType);
    }

    public void deserialize(ByteBuffer buffer) {
        priority = buffer.getShort();
        messageType = buffer.getInt();
    }
}
