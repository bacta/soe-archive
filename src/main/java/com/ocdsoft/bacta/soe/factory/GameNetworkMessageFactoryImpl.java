package com.ocdsoft.bacta.soe.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import io.netty.util.collection.IntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by kyle on 4/17/2016.
 */
@Singleton
public class GameNetworkMessageFactoryImpl implements GameNetworkMessageFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameNetworkMessageFactoryImpl.class);

    private final static int OBJECT_CONTROLLER_MESSAGE = 0x80CE5E46;
    private final static int COMMAND_CONTROLLER_MESSAGE = 0x116;

    private final Injector injector;

    private final IntObjectHashMap<Class<? extends GameNetworkMessage>> messageClassMap;

    @Inject
    public GameNetworkMessageFactoryImpl(final Injector injector) {
        this.injector = injector;
        this.messageClassMap = new IntObjectHashMap<>();
    }

    @Override
    public GameNetworkMessage create(Class<? extends GameNetworkMessage> messageClass) {
        return injector.getInstance(messageClass);
    }

    @Override
    public GameNetworkMessage createAndDeserialize(int gameMessageType, ByteBuffer buffer) throws NullPointerException {

        if ( gameMessageType == OBJECT_CONTROLLER_MESSAGE ) {

            gameMessageType = buffer.getInt(10);

            if ( gameMessageType == COMMAND_CONTROLLER_MESSAGE ) {
                gameMessageType = buffer.getInt(30);
            }
        }

        Class<? extends GameNetworkMessage> messageClass = messageClassMap.get(gameMessageType);

        GameNetworkMessage messageInstance = create(messageClass);
        if(messageInstance == null) {
            throw new GameNetworkMessageTypeNotFoundException(gameMessageType);
        }
        messageInstance.readFromBuffer(buffer);

        return messageInstance;
    }

    @Override
    public void addHandledMessageClass(int hash, Class<? extends GameNetworkMessage> handledMessageClass) {

        if(messageClassMap.containsKey(hash)) {
            LOGGER.error("Message already exists in class map {}", handledMessageClass.getSimpleName());
        }

        LOGGER.debug("Putting {} {} in message factory", hash, handledMessageClass.getName());
        messageClassMap.put(hash, handledMessageClass);
    }
}
