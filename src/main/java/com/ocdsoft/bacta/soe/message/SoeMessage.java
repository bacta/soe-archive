package com.ocdsoft.bacta.soe.message;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class SoeMessage {

    @Getter
    protected transient boolean compressed = true;

    @Getter
    protected final byte zeroByte;

    @Getter
    protected final UdpPacketType packetType;

    protected transient final ByteBuffer buffer;


    public SoeMessage(UdpPacketType packetType) {
        buffer = ByteBuffer.allocate(496).order(ByteOrder.BIG_ENDIAN);

        this.zeroByte = 0;
        this.packetType = packetType;

        buffer.put(zeroByte);
        packetType.writeToBuffer(buffer);
    }

    public SoeMessage(UdpPacketType packetType, ByteBuffer buffer) {
        this.buffer = buffer;

        this.zeroByte = 0;
        this.packetType = packetType;

        buffer.put(zeroByte);
        packetType.writeToBuffer(buffer);
    }

    public ByteBuffer slice() {
        buffer.limit(buffer.position());
        buffer.rewind();
        return buffer.slice();
    }

    public int size() {
        return buffer.limit();
    }
}
