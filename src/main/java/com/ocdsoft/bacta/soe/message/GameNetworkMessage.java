package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.engine.buffer.ByteBufferWritable;

/**
 * Created by Kyle on 3/26/14.
 */
public interface GameNetworkMessage extends ByteBufferWritable {
    short getPriority();
    int getMessageType();
}
