package com.ocdsoft.bacta.soe.factory;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

import java.nio.ByteBuffer;

/**
 * A factory the generates all of the {@link GameNetworkMessage} instances
 */
public interface GameNetworkMessageFactory {

    /**
     * Creates a message of the specified type and deserializes provided buffer
     * into new instance.  The message type can be the {@link GameNetworkMessage} type,
     * the ObjControllerMessage header or the Command name hash
     * @param gameMessageType the key to lookup the message class
     * @param buffer Incoming buffer to deserialize from
     * @return new instance of the {@link GameNetworkMessage} specified with deserialized
     *      data provided.
     * @throws NullPointerException if message type is invalid
     */
    GameNetworkMessage create(int gameMessageType, final ByteBuffer buffer) throws NullPointerException;

    /**
     * Builds the map of messages handled
     * @param hash The {@link com.ocdsoft.bacta.soe.util.SOECRC32#hashCode(String)} value of the
     *             incoming message
     * @param handledMessageClass the class object of the message to be added to the handler
     */
    void addHandledMessageClass(int hash, Class<? extends GameNetworkMessage> handledMessageClass);
}
