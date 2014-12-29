package com.ocdsoft.bacta.soe.client;

import com.ocdsoft.bacta.engine.network.client.ClientState;
import com.ocdsoft.bacta.engine.network.client.UdpClient;
import com.ocdsoft.bacta.engine.network.client.UdpMessageProcessor;
import com.ocdsoft.bacta.soe.message.AckMessage;
import com.ocdsoft.bacta.soe.message.Disconnect;
import com.ocdsoft.bacta.soe.message.SoeMessage;
import com.ocdsoft.bacta.soe.utils.SoeMessageUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class SoeUdpClient extends UdpClient {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private static ResourceBundle messageProperties;

    private ClientState clientState = ClientState.DISCONNECTED;

    static {
        messageProperties = ResourceBundle.getBundle("messageprocessing");
    }

    @Getter
    @Setter
    private int connectionId;
    @Getter
    @Setter
    private int udpSize;
    @Getter
    @Setter
    private int sessionKey;
    @Getter
    @Setter
    private int accountId;
    @Getter
    @Setter
    private String accountUsername;

    private final int staleTimeout;

    private final UdpMessageProcessor<ByteBuffer> udpMessageProcessor;

    private final AtomicInteger clientSequenceNumber = new AtomicInteger();

    @Getter
    private long lastActivity = System.currentTimeMillis();

    public SoeUdpClient() {
        udpMessageProcessor = new SoeUdpMessageProcessor(this, messageProperties);
        staleTimeout = Integer.parseInt(messageProperties.getString("staleDisconnect"));
    }

    public void sendMessage(SoeMessage buffer) {

        if (udpMessageProcessor.addUnreliable(buffer)) {
            lastActivity = System.currentTimeMillis();
        }
    }

    public void sendMessage(SwgMessage buffer) {

        if (!udpMessageProcessor.addReliable(buffer)) {
            if(getClientState() == ClientState.ONLINE) {
                disconnect();
            }
        } else {
            lastActivity = System.currentTimeMillis();
        }
    }

    public List<ByteBuffer> getPendingMessages() {

        List<ByteBuffer> pendingMessageList = new ArrayList<>();

        ByteBuffer buffer;
        while ((buffer = udpMessageProcessor.processNext()) != null) {
            pendingMessageList.add(buffer);
            logger.trace("Sending: " + SoeMessageUtil.bytesToHex(buffer));
        }

        return pendingMessageList;
    }

    public void sendAck(short sequenceNum) {
        lastActivity = System.currentTimeMillis();
        sendMessage(new AckMessage(sequenceNum));
    }

    public void sendErrorMessage(String type, String message, boolean fatal) {
        sendMessage(new ErrorMessage(type, message, fatal));
    }

    public void processAck(short sequenceNum) {
        clientSequenceNumber.set(sequenceNum);
        udpMessageProcessor.acknowledge(sequenceNum);
    }

    @Override
    public void disconnect() {

        Disconnect disconnect = new Disconnect(getConnectionId(), Disconnect.NONE);
        sendMessage(disconnect);

        lastActivity = System.currentTimeMillis();
        setClientState(ClientState.DISCONNECTED);
    }

    /**
     * Idle timeout
     *
     * @return
     */
    public boolean isStale() {
        return (System.currentTimeMillis() - lastActivity > (staleTimeout));
    }

    @Synchronized("clientState")
    public void setClientState(ClientState state) {
        clientState = state;
    }

    @Synchronized("clientState")
    public ClientState getClientState() {
        return clientState;
    }

    public void close() {
    }
}
