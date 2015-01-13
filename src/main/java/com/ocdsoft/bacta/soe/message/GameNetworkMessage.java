package com.ocdsoft.bacta.soe.message;

import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * Created by Kyle on 3/26/14.
 */
public abstract class GameNetworkMessage {

    @Getter
    private final short priority;

    @Getter
    private final int messageType;

    public GameNetworkMessage(int priority, int messageType) {
        this.priority = (short) priority;
        this.messageType = messageType;
    }

    public abstract void serialize(ByteBuffer buffer);

    public abstract void deserialize(ByteBuffer buffer);
}
