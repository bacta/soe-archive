package com.ocdsoft.bacta.soe.connection;

import com.ocdsoft.bacta.engine.network.client.UdpMessageBuilder;
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration;
import com.ocdsoft.bacta.soe.message.MultiMessage;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Kyle on 3/28/14.
 */
public class SoeUdpMessageBuilder implements UdpMessageBuilder<ByteBuffer> {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final Queue<ByteBuffer> bufferList;
    private final NetworkConfiguration configuration;
    private MultiMessage pendingMulti;
    private ByteBuffer pendingBuffer;

    public SoeUdpMessageBuilder(final NetworkConfiguration configuration) {
        this.configuration = configuration;
        bufferList = new ArrayBlockingQueue<>(configuration.getMaxOutstandingPackets());
        pendingMulti = null;
        pendingBuffer = null;
    }

    @Override
    public synchronized boolean add(ByteBuffer buffer) {

        if(!configuration.isMultiSoeMessages()) {
            return bufferList.add(buffer);
        }

        logger.trace("Adding: " + SoeMessageUtil.bytesToHex(buffer));
        logger.trace("  Queue Size: " + bufferList.size());
        logger.trace("  Pending Multi: " + (pendingMulti != null));
        logger.trace("  Pending Buffer: " + (pendingBuffer != null));

        if(pendingMulti != null) {
            if(pendingMulti.size() + buffer.remaining() <= configuration.getMaxMultiPayload()) {
                pendingMulti.add(buffer);
                logger.trace("Appending: " + SoeMessageUtil.bytesToHex(buffer));
            } else {
                ByteBuffer send = pendingMulti.slice();
                bufferList.add(send);
                logger.trace("Ready to send: " + SoeMessageUtil.bytesToHex(send));
                pendingMulti = null;
                pendingBuffer = buffer;
            }
        } else {
            if(pendingBuffer != null) {
                if(pendingBuffer.remaining() + buffer.remaining() <= configuration.getMaxMultiPayload()) {
                    pendingMulti = new MultiMessage(pendingBuffer, buffer);
                    logger.trace("Combining: " + SoeMessageUtil.bytesToHex(pendingBuffer));
                    logger.trace("Combining: " + SoeMessageUtil.bytesToHex(buffer));
                    pendingBuffer = null;
                } else {
                    bufferList.add(pendingBuffer);
                    pendingBuffer = buffer;
                }
            }  else {
                pendingBuffer = buffer;
            }
        }

        logger.trace("  Queue Size: " + bufferList.size());
        logger.trace("  Pending Multi: " + (pendingMulti != null));
        logger.trace("  Pending Buffer: " + (pendingBuffer != null));
        return true;
    }

    @Override
    public synchronized ByteBuffer buildNext() {

        ByteBuffer buffer = bufferList.poll();

        if(buffer == null && pendingMulti != null) {
            buffer = pendingMulti.slice();
            pendingMulti = null;
        }
        if(buffer == null) {
            buffer = pendingBuffer;
            pendingBuffer = null;
        }

        return buffer;
    }

    @Override
    public void acknowledge(short sequenceNumber) {

    }
}
