package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.util.SOECRC32;
import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * Created by crush on 2/1/15.
 */
public class ChatAgentIdentity extends GameNetworkMessage {
    private static final short priority = 0x4;
    private static final int messageType = SOECRC32.hashCode(ChatAgentIdentity.class.getSimpleName());

    @Getter
    private final String name;
    @Getter
    private final String address;
    @Getter
    private final short port;

    public ChatAgentIdentity(final String name, final String address, final short port) {
        super(priority, messageType);

        this.name = name;
        this.address = address;
        this.port = port;
    }

    public ChatAgentIdentity(final ByteBuffer buffer) {
        super(priority, messageType);

        this.name = BufferUtil.getAscii(buffer);
        this.address = BufferUtil.getAscii(buffer);
        this.port = buffer.getShort();
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        BufferUtil.putAscii(buffer, name);
        BufferUtil.putAscii(buffer, address);
        buffer.putShort(port);
    }
}
