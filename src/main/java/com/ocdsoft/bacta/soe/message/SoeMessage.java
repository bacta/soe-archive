package com.ocdsoft.bacta.soe.message;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class SoeMessage {

    @Getter
    protected transient boolean compressed = true;

    @Getter
    protected transient boolean process = true;

    @Getter
    protected final byte zeroByte;

    @Getter
    protected final UdpPacketType packetType;

    protected transient final ByteBuffer buffer;

//    protected SoeMessage() {
//        buffer = ByteBuffer.allocate(496);
//    }

    public SoeMessage(UdpPacketType packetType) {
        buffer = ByteBuffer.allocate(496).order(ByteOrder.BIG_ENDIAN);

        this.zeroByte = 0;
        this.packetType = packetType;
    }

    public SoeMessage(UdpPacketType packetType, ByteBuffer buffer) {
        this.zeroByte = 0;
        this.packetType = packetType;
        this.buffer = buffer;
    }
}
