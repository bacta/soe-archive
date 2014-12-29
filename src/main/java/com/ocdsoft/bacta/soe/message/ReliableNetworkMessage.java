package com.ocdsoft.bacta.soe.message;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class ReliableNetworkMessage extends SoeMessage {

    @Getter
    private final int sequenceNumber;

    @Getter
    private int sendAttempts = 0;

    @Getter
    private long lastSendAttempt = 0;

    private final List<ByteBuf> list = new ArrayList<>();

    /**
     * This constuctor is use for standard reliable messages
     *
     * @param sequenceNumber
     * @param buffer
     */
    public ReliableNetworkMessage(int sequenceNumber, ByteBuf buffer) {
        super(0x9);
        this.sequenceNumber = sequenceNumber;
        writeShortBE(sequenceNumber);
        list.add(buffer);
    }

    /**
     * This constructor is used for reliable fragments
     *
     * @param sequenceNumber
     * @param buffer
     */
    public ReliableNetworkMessage(int sequenceNumber, ByteBuf buffer, boolean first, int size) {
        super(0x0D);
        this.sequenceNumber = sequenceNumber;
        writeShortBE(sequenceNumber);
        if(first) {
            writeIntBE(size);
        }
        list.add(buffer);
    }

    public int size() {
        // for 0x19
        int size = 2;

        for (ByteBuf buffer : list) {
            int sizeCount = (buffer.readableBytes() / 0xFF) - (buffer.readableBytes() % 0xFF == 0 ? 1 : 0) + 1;
            if(sizeCount > 1) {
                sizeCount += 1;
            }
            size += buffer.readableBytes() + sizeCount;
        }
        return size;
    }


    public boolean addMessage(ByteBuf buffer) {

        return list.add(buffer);
    }

    public ReliableNetworkMessage finish() {
        if (list.size() == 1) {
            writeBytes(list.get(0));
        } else {
            writeShortBE(0x19);
            for (ByteBuf buffer : list) {
                int byteCount = buffer.readableBytes();
                if(byteCount > 0xFF) {
                    int sizeCount = (byteCount / 0xFF) - (byteCount % 0xFF == 0 ? 1 : 0);

                    writeByte(0xFF);
                    writeByte(sizeCount);
                    byteCount -= 0xFF;

                    for (int i = 0; i < sizeCount; ++i) {
                        writeByte(byteCount > 0xFF ? 0xFF : byteCount);
                        byteCount -= 0xFF;
                    }

                } else {
                    writeByte(byteCount);
                }

                writeBytes(buffer);
            }
        }
        return this;
    }

    public void addSendAttempt() {
        sendAttempts++;
        lastSendAttempt = System.currentTimeMillis();
    }

    @Override
    public int compareTo(ByteBuf o) {
        ReliableNetworkMessage message = (ReliableNetworkMessage)o;
        if(message == null) {
            return super.compareTo(o);
        }

        return getSequenceNumber() - message.getSequenceNumber();
    }
}
