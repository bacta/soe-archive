package com.ocdsoft.bacta.soe.serialize;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.dispatch.MessageId;
import com.ocdsoft.bacta.soe.message.CommandMessage;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.message.Priority;
import com.ocdsoft.bacta.soe.util.MessageHashUtil;
import com.ocdsoft.bacta.soe.util.SOECRC32;
import io.netty.util.collection.IntObjectHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by kyle on 5/4/2016.
 */
@Singleton
public class GameNetworkMessageSerializerImpl implements GameNetworkMessageSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameNetworkMessageSerializerImpl.class);

    private final IntObjectHashMap<Constructor<? extends GameNetworkMessage>> messageConstructorMap;
    private final Map<Class<? extends GameNetworkMessage>, MessageData> messageDataMap;

    public GameNetworkMessageSerializerImpl() {
        messageDataMap = new HashMap<>();
        this.messageConstructorMap = new IntObjectHashMap<>();
        loadMessages();
    }

    private void loadMessages() {

        Reflections reflections = new Reflections();
        Set<Class<? extends GameNetworkMessage>> subTypes = reflections.getSubTypesOf(GameNetworkMessage.class);
        Iterator<Class<? extends GameNetworkMessage>> iter = subTypes.iterator();

        while (iter.hasNext()) {

            Class<? extends GameNetworkMessage> messageClass = iter.next();
            loadMessageClass(messageClass);
        }
    }

    private void loadMessageClass(Class<? extends GameNetworkMessage> messageClass) {

        final int hash = MessageHashUtil.getHash(messageClass);

        if(messageConstructorMap.containsKey(hash)) {
            LOGGER.error("Message already exists in class map {}", messageClass.getSimpleName());
        }

        try {
            Constructor<GameNetworkMessage> constructor = (Constructor<GameNetworkMessage>) messageClass.getConstructor(ByteBuffer.class);

            LOGGER.debug("Putting {} {} in message factory", hash, messageClass.getName());
            messageConstructorMap.put(hash, constructor);

        } catch (NoSuchMethodException e) {
            LOGGER.error("{} does not have a constructor taking ByteBuffer", messageClass.getName(), e);
        }

    }

    @Override
    public <T extends GameNetworkMessage> ByteBuffer writeToBuffer(T message) {

        // TODO: Better buffer player
        ByteBuffer buffer = ByteBuffer.allocate(1500).order(ByteOrder.LITTLE_ENDIAN);

        MessageData data = messageDataMap.get(message.getClass());
        if (data == null) {
            Priority priority = message.getClass().getAnnotation(Priority.class);
            short value;
            if(priority == null) {
                value = 0x2;
            } else {
                value = priority.value();
            }
            data = new MessageData(
                    value,
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
            LOGGER.error("Unable to construct message {}", messageConstructor.getName(), e);
            return null;
        }
    }

    @Override
    public GameNetworkMessage readFromBuffer(int gameMessageType, final ByteBuffer buffer) {

        final Constructor<? extends GameNetworkMessage> messageConstructor = messageConstructorMap.get(gameMessageType);

        if(messageConstructor == null) {
            throw new GameNetworkMessageTypeNotFoundException(gameMessageType);
        }
        GameNetworkMessage messageInstance = create(messageConstructor, buffer);
        return messageInstance;
    }

    @Getter
    @AllArgsConstructor
    private final class MessageData {
        private final short priority;
        private final int type;
    }
}
