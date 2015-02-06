package com.ocdsoft.bacta.soe.connection;

import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.engine.network.client.UdpMessageBuilder;
import com.ocdsoft.bacta.soe.message.ReliableNetworkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Kyle on 3/26/14.
 */

/**
 struct UdpManager::ReliableConfig
 {
     int maxOutstandingBytes;
     int maxOutstandingPackets;
     int maxInstandingPackets;
     int fragmentSize;
     int trickleSize;
     int trickleRate;
     int resendDelayAdjust;
     int resendDelayPercent;
     int resendDelayCap;
     int congestionWindowMinimum;
     bool outOfOrder;
     bool processOnSend;
     bool coalesce;
     bool ackDeduping;
 };
 */
public class ReliableUdpMessageBuilder implements UdpMessageBuilder<ByteBuffer> {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicInteger sequenceNum = new AtomicInteger();
    private final Set<ReliableNetworkMessage> containerList;
    private final int maxQueueSize;
    private final int udpMaxReliablePayload;
    private final int udpMaxFragmentedPayload;
    private final int unacknowledgedLimit;
    private long lastAck;
    private long lastSend;
    private int resendDelay;
    private float resendDelayPercentage;
    private int noDataTimeout;
    private final SoeUdpConnection connection;

    private ReliableNetworkMessage pendingContainer;
    private boolean combineGameMessages;

    private final Queue<ReliableNetworkMessage> unacknowledgedQueue;

    public ReliableUdpMessageBuilder(SoeUdpConnection connection, final ResourceBundle messageProperties) {

        this.connection = connection;
        int udpMaxSize = Integer.parseInt(messageProperties.getString("UdpMaxSize"));
        int footerLength = Integer.parseInt(messageProperties.getString("FooterLength"));
        this.udpMaxReliablePayload = udpMaxSize - footerLength - 4;
        this.udpMaxFragmentedPayload = udpMaxSize - footerLength - 8;

        this.resendDelay = Integer.parseInt(messageProperties.getString("ResendDelayAdjust"));
        this.resendDelayPercentage = Float.parseFloat(messageProperties.getString("ResendDelayAdjust")) / 100.f;

        this.maxQueueSize = Integer.parseInt(messageProperties.getString("MaxQueueSize"));
        this.unacknowledgedLimit = Integer.parseInt(messageProperties.getString("UnacknowledgedLimit"));
        this.noDataTimeout = Integer.parseInt(messageProperties.getString("noDataTimeout"));
        this.combineGameMessages = Boolean.parseBoolean(messageProperties.getString("MultiGameMessages"));
        lastAck = 0;
        lastSend = 0;

        containerList = Collections.synchronizedSet(new TreeSet<ReliableNetworkMessage>());
        pendingContainer = null;

        unacknowledgedQueue = new PriorityBlockingQueue<>(unacknowledgedLimit);
    }

    private short getAndIncrement() {

        int value = sequenceNum.getAndIncrement();
        if(value > Short.MAX_VALUE) {
            value = 0;
            sequenceNum.set(value);
        }

        return (short) value;
    }

    @Override
    public synchronized boolean add(ByteBuffer buffer) {

        if (unacknowledgedQueue.size() >= unacknowledgedLimit) {
            return false;
        }

        if (pendingContainer != null) {
            if (combineGameMessages && pendingContainer.size() + buffer.limit() + 1 <= udpMaxReliablePayload) {
                return pendingContainer.addMessage(buffer);
            }
            pendingContainer.finish();
            containerList.add(pendingContainer);
            pendingContainer = null;
        }

        // Fragment large message
        if (buffer.limit() > udpMaxReliablePayload) {

            if(pendingContainer != null) {
                pendingContainer.finish();
                containerList.add(pendingContainer);
                pendingContainer = null;
            }

            //TODO: Pool instances?
            FragmentProcessor fragmentProcessor = new FragmentProcessor(buffer);
            while (fragmentProcessor.hasNext()) {
                containerList.add(fragmentProcessor.next());
            }

            return true;
        }

        pendingContainer = new ReliableNetworkMessage(getAndIncrement(), buffer);
        return true;
    }

    @Override
    public synchronized ByteBuffer buildNext() {

        long currentTime = System.currentTimeMillis();

        if(currentTime - connection.getLastActivity() > noDataTimeout) {
            connection.setState(ConnectionState.LINKDEAD);
        }
//
//        for(ReliableNetworkMessage message : unacknowledgedQueue) {
//            if(currentTime - message.getLastSendAttempt() > 10000 + message.getSendAttempts() * (resendDelayPercentage * resendDelay)) {
//                message.addSendAttempt();
//                return message.getBuffer().slice();
//            }
//        }

        Iterator<ReliableNetworkMessage> iterator = containerList.iterator();

        if (!iterator.hasNext()) {
            if (pendingContainer != null) {
                pendingContainer.finish();
                pendingContainer.addSendAttempt();
                unacknowledgedQueue.add(pendingContainer);
                ByteBuffer slice = pendingContainer.slice();
                pendingContainer = null;
                lastSend = currentTime;
                return slice;
            }
            return null;
        }

        ReliableNetworkMessage message = iterator.next();
        containerList.remove(message);
        message.addSendAttempt();
        unacknowledgedQueue.add(message);
        lastSend = currentTime;
        return message.slice();
    }

    @Override
    public void acknowledge(short sequenceNumber) {
        logger.debug("Client Ack: " + sequenceNumber);
        lastAck = System.currentTimeMillis();
        while (!unacknowledgedQueue.isEmpty() &&
                (unacknowledgedQueue.peek().getSequenceNumber() <= sequenceNumber)) {
            unacknowledgedQueue.poll();
        }

    }

    private class FragmentProcessor {

        private final ByteBuffer buffer;
        private final int size;
        private boolean first;

        public FragmentProcessor(ByteBuffer buffer) {
            this.buffer = buffer;
            size = buffer.remaining();
            first = true;
        }

        public boolean hasNext() {
            return buffer.hasRemaining();
        }

        public ReliableNetworkMessage next() {

            int messageSize;
            if (buffer.remaining() > udpMaxFragmentedPayload) {
                messageSize = udpMaxFragmentedPayload;
            } else {
                messageSize = buffer.remaining();
            }

            ByteBuffer slice = buffer.slice();
            slice.limit(messageSize);

            buffer.position(buffer.position() + messageSize);

            ReliableNetworkMessage message = new ReliableNetworkMessage(getAndIncrement(), slice, first, size);
            message.finish();

            first = false;
            return message;
        }
    }
}
