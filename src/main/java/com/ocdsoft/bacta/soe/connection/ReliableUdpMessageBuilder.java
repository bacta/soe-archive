package com.ocdsoft.bacta.soe.connection;

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
public class ReliableUdpMessageBuilder implements UdpMessageBuilder<ByteBuffer> {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicInteger sequenceNumber = new AtomicInteger();
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
    private final SoeUdpConnection client;

    private ReliableNetworkMessage pendingContainer;
    private boolean combineGameMessages;

    private final Queue<ReliableNetworkMessage> unacknowledgedQueue;

    public ReliableUdpMessageBuilder(SoeUdpConnection client, final ResourceBundle messageProperties) {

        this.client = client;
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

    @Override
    public synchronized boolean add(ByteBuffer buffer) {

        if (unacknowledgedQueue.size() >= unacknowledgedLimit) {
            return false;
        }

        if (pendingContainer != null) {
            if (combineGameMessages && pendingContainer.size() + buffer.readableBytes() + 1 <= udpMaxReliablePayload) {
                return pendingContainer.addMessage(buffer);
            }
            containerList.add(pendingContainer.finish());
            pendingContainer = null;
        }

        // Fragment large message
        if (buffer.readableBytes() > udpMaxReliablePayload) {

            if(pendingContainer != null) {
                containerList.add(pendingContainer.finish());
                pendingContainer = null;
            }

            //TODO: Pool instances?
            FragmentProcessor fragmentProcessor = new FragmentProcessor(buffer);
            while (fragmentProcessor.hasNext()) {
                containerList.add(fragmentProcessor.next());
            }

            return true;
        }

        pendingContainer = new ReliableNetworkMessage(sequenceNumber.getAndIncrement(), buffer);
        return true;
    }

    @Override
    public synchronized ByteBuffer buildNext() {

        long currentTime = System.currentTimeMillis();

        if(currentTime - client.getLastActivity() > noDataTimeout) {
            client.setLinkDead();
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
                ReliableNetworkMessage send = pendingContainer.finish();
                pendingContainer = null;
                send.addSendAttempt();
                unacknowledgedQueue.add(send);
                lastSend = currentTime;
                return send.slice();
            }
            return null;
        }

        ReliableNetworkMessage buffer = iterator.next();
        containerList.remove(buffer);
        buffer.addSendAttempt();
        unacknowledgedQueue.add(buffer);
        lastSend = currentTime;
        return buffer.slice();
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
            size = buffer.readableBytes();
            first = true;
        }

        public boolean hasNext() {
            return buffer.readableBytes() > 0;
        }

        public ReliableNetworkMessage next() {

            int messageSize;
            if (buffer.readableBytes() > udpMaxFragmentedPayload) {
                messageSize = udpMaxFragmentedPayload;
            } else {
                messageSize = buffer.readableBytes();
            }

            ByteBuffer slice = buffer.slice(buffer.readerIndex(), messageSize);
            buffer.readerIndex(buffer.readerIndex() + messageSize);

            ReliableNetworkMessage message = new ReliableNetworkMessage(sequenceNumber.getAndIncrement(), slice, first, size).finish();
            first = false;
            return message;
        }
    }
}
