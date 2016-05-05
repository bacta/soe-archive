package com.ocdsoft.bacta.soe.serialize;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.message.Priority;
import com.ocdsoft.bacta.soe.util.SOECRC32;
import io.netty.util.collection.IntObjectHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kyle on 5/4/2016.
 */
@Singleton
public class GameNetworkMessageSerializerImpl implements GameNetworkMessageSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameNetworkMessageSerializerImpl.class);

    private final static int OBJECT_CONTROLLER_MESSAGE = 0x80CE5E46;
    private final static int COMMAND_CONTROLLER_MESSAGE = 0x116;

    private final IntObjectHashMap<Constructor<? extends GameNetworkMessage>> messageConstructorMap;

    private final Map<Class<? extends GameNetworkMessage>, MessageData> messageDataMap;

    public GameNetworkMessageSerializerImpl() {
        messageDataMap = new HashMap<>();
        this.messageConstructorMap = new IntObjectHashMap<>();
    }

    @Override
    public <T extends GameNetworkMessage> ByteBuffer writeToBuffer(T message) {

        // TODO: Better buffer creation
        ByteBuffer buffer = ByteBuffer.allocate(1500).order(ByteOrder.LITTLE_ENDIAN);

        MessageData data = messageDataMap.get(message.getClass());
        if (data == null) {
            Priority priority = message.getClass().getAnnotation(Priority.class);
            data = new MessageData(
                    priority.value(),
                    SOECRC32.hashCode(message.getClass().getSimpleName())
            );
            messageDataMap.put(message.getClass(), data);
        }

        buffer.putShort(data.getPriority());
        buffer.putInt(data.getType());

        message.writeToBuffer(buffer);
        buffer.limit(buffer.position());
        buffer.rewind();

        return buffer;
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
    public GameNetworkMessage readFromBuffer(int gameMessageType, final ByteBuffer buffer) {

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

    @Getter
    @AllArgsConstructor
    private final class MessageData {
        private final short priority;
        private final int type;
    }
}
