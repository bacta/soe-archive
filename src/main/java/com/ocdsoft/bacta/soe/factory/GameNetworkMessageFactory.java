package com.ocdsoft.bacta.soe.factory;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

/**
 * Created by kyle on 4/11/2016.
 */
public interface GameNetworkMessageFactory {
    GameNetworkMessage create(int opcode, ByteBuffer buffer);
}
