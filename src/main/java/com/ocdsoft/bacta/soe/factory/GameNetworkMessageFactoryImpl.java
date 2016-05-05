package com.ocdsoft.bacta.soe.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import io.netty.util.collection.IntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

/**
 * Created by kyle on 4/17/2016.
 */
@Singleton
public class GameNetworkMessageFactoryImpl implements GameNetworkMessageFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameNetworkMessageFactoryImpl.class);

    private final static int OBJECT_CONTROLLER_MESSAGE = 0x80CE5E46;
    private final static int COMMAND_CONTROLLER_MESSAGE = 0x116;

    private final IntObjectHashMap<Constructor<? extends GameNetworkMessage>> messageConstructorMap;

    @Inject
    public GameNetworkMessageFactoryImpl() {
        this.messageConstructorMap = new IntObjectHashMap<>();
    }

    private GameNetworkMessage create(final Constructor<? extends GameNetworkMessage> messageConstructor, final ByteBuffer buffer) {
        try {

            return messageConstructor.newInstance(buffer);

        } catch (Exception e) {
            LOGGER.error("Unable to construct message {}", messageConstructor.getName());
            return null;
        }
    }

    @Override
    public GameNetworkMessage create(int gameMessageType, final ByteBuffer buffer) throws NullPointerException {

        if ( gameMessageType == OBJECT_CONTROLLER_MESSAGE ) {

            gameMessageType = buffer.getInt(10);

            if ( gameMessageType == COMMAND_CONTROLLER_MESSAGE ) {
                gameMessageType = buffer.getInt(30);
            }
        }

        final Constructor<? extends GameNetworkMessage> messageClass = messageConstructorMap.get(gameMessageType);

        GameNetworkMessage messageInstance = create(messageClass, buffer);
        if(messageInstance == null) {
            throw new GameNetworkMessageTypeNotFoundException(gameMessageType);
        }

        return messageInstance;
    }

    @Override
    public void addHandledMessageClass(int hash, Class<? extends GameNetworkMessage> handledMessageClass) {

        if(messageConstructorMap.containsKey(hash)) {
            LOGGER.error("Message already exists in class map {}", handledMessageClass.getSimpleName());
        }

        try {
            Constructor<GameNetworkMessage> constructor = (Constructor<GameNetworkMessage>) handledMessageClass.getConstructor(ByteBuffer.class);

            LOGGER.debug("Putting {} {} in message factory", hash, handledMessageClass.getName());
            messageConstructorMap.put(hash, constructor);

        } catch (NoSuchMethodException e) {
            LOGGER.error("{} does not have a constructor taking ByteBuffer", handledMessageClass.getName(), e);
        }


    }
}
