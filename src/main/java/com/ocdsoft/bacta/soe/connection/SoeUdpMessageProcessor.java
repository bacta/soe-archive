package com.ocdsoft.bacta.soe.connection;

import com.ocdsoft.bacta.engine.network.client.ClientState;
import com.ocdsoft.bacta.engine.network.client.UdpMessageBuilder;
import com.ocdsoft.bacta.engine.network.client.UdpMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ResourceBundle;

/**
 * @author kyle
 */
public final class SoeUdpMessageProcessor implements UdpMessageProcessor<ByteBuffer> {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final UdpMessageBuilder<ByteBuffer> udpMessageBuilder;
    private final UdpMessageBuilder<ByteBuffer> reliableUdpMessageBuilder;

    private final int udpMaxSize;
    private final SoeUdpConnection client;

    public SoeUdpMessageProcessor(SoeUdpConnection client, final ResourceBundle messageProperties) {

        this.client = client;

        this.udpMaxSize = Integer.parseInt(messageProperties.getString("UdpMaxSize"));
        int footerLength = Integer.parseInt(messageProperties.getString("FooterLength"));

        int udpMaxMultiPayload = udpMaxSize - footerLength - 3;

        reliableUdpMessageBuilder = new ReliableUdpMessageBuilder(client, messageProperties);
        udpMessageBuilder = new SoeUdpMessageBuilder(udpMaxMultiPayload, messageProperties);
    }

    @Override
    public boolean addReliable(ByteBuffer buffer) {
        if (buffer == null) throw new NullPointerException();

        return reliableUdpMessageBuilder.add(buffer);
    }

    @Override
    public boolean addUnreliable(ByteBuffer buffer) {
        if (buffer == null) throw new NullPointerException();

        flushReliable();
        return udpMessageBuilder.add(buffer);
    }

    @Override
    public ByteBuffer processNext() {

        flushReliable();
        ByteBuffer message = udpMessageBuilder.buildNext();
        if (message != null && message.readableBytes() > udpMaxSize) {
            throw new RuntimeException("Sending packet that exceeds " + udpMaxSize + " bytes");
        }
        return message;

    }

    @Override
    public void acknowledge(short reliableSequence) {
        if(client.getClientState() != ClientState.ONLINE) {
            client.setClientState(ClientState.ONLINE);
        }
        reliableUdpMessageBuilder.acknowledge(reliableSequence);
    }

    private void flushReliable() {
        ByteBuffer message;
        while ((message = reliableUdpMessageBuilder.buildNext()) != null) {
            udpMessageBuilder.add(message);
        }
    }
}
