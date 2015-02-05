package com.ocdsoft.bacta.soe.io.udp;

import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.engine.network.io.udp.UdpTransceiver;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kburkhardt on 2/15/14.
 */

public final class SoeTransceiver extends UdpTransceiver<SoeUdpConnection> {

    private final Logger logger = LoggerFactory.getLogger(SoeTransceiver.class);

    private final ServerType serverType;

    private final SoeMessageRouter soeMessageRouter;

    private final SoeProtocol protocol;

    /**
     * The structure that holds the "connected" udp clients
     */
    @Getter
    private final Map<Object, SoeUdpConnection> connectionMap;

    private final Thread sendThread;
    private final int sendQueueInterval;

    private final Collection<String> whitelistedAddresses;

    public SoeTransceiver(final InetAddress bindAddress,
                          final int port,
                          final ServerType serverType,
                          final int sendQueueInterval,
                          final SoeMessageRouter soeMessageRouter,
                          final Collection<String> whitelistedAddresses) {

        super(bindAddress, port);

        try {

            this.serverType = serverType;
            this.sendQueueInterval = sendQueueInterval;
            this.soeMessageRouter = soeMessageRouter;
            this.protocol = new SoeProtocol();
            this.whitelistedAddresses = whitelistedAddresses;

            ResourceBundle bundle = PropertyResourceBundle.getBundle("messageprocessing");
            protocol.setCompression(bundle.getString("Compression").equalsIgnoreCase("true"));

            connectionMap = new ConcurrentHashMap<>();

            sendThread = new Thread(new SendLoop());
            sendThread.setName(serverType.name() + " Send Thread");
            sendThread.start();

        } catch (SecurityException e) {

            throw new RuntimeException("Unable to start SOE transceiver", e);
        }
    }

    /**
     * The factory method that creates instances of the {@link com.ocdsoft.bacta.engine.network.client.UdpConnection} specified in the {@code Client} parameter
     *
     * @param address {@link java.net.InetSocketAddress of incoming {@code Data} message
     * @return New instance of user specified class {@code Connection}
     * @throws Exception
     * @since 1.0
     */
    private final SoeUdpConnection createConnection(InetSocketAddress address) throws RuntimeException {
        SoeUdpConnection connection;
        try {
            connection = new SoeUdpConnection();
            if(whitelistedAddresses != null && whitelistedAddresses.contains(address.getHostString())) {
                connection.addRole(ConnectionRole.WHITELISTED);
                logger.info("Whitelisted address connected: " + address.getHostString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        connection.setRemoteAddress(address);
        return connection;
    }

    public final void createOutgoingConnection(final SoeUdpConnection connection) throws RuntimeException {
        try {
            if(whitelistedAddresses != null && whitelistedAddresses.contains(connection.getRemoteAddress().getHostString())) {
                connection.addRole(ConnectionRole.WHITELISTED);
                logger.debug("Whitelisted address connected: " + connection.getRemoteAddress().getHostString());
            }

            connectionMap.put(connection.getRemoteAddress(), connection);

            logger.debug("{} connection to {} now has {} total connected clients.",
                    connection.getClass().getSimpleName(),
                    connection.getRemoteAddress(),
                    connectionMap.size());
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void receiveMessage(InetSocketAddress sender, ByteBuffer buffer) {

        try {

            SoeUdpConnection connection = connectionMap.get(sender);

            byte type = buffer.get(1);
            if(type < 0 || type > 0x1E) {
                throw new RuntimeException("Type out of range:" + type);
            }

            UdpPacketType packetType = UdpPacketType.values()[type];

            if (packetType == UdpPacketType.cUdpPacketConnect) {

                connection = createConnection(sender);
                connectionMap.put(sender, connection);

                logger.debug("{} connection from {} now has {} total connected clients.",
                        connection.getClass().getSimpleName(),
                        sender,
                        connectionMap.size());
                
            } else  {

                if(connection == null) {
                    logger.debug("Unsolicited Message from " + sender + ": " + BufferUtil.bytesToHex(buffer));
                    return;
                }

                if (packetType != UdpPacketType.cUdpPacketConfirm) {
                    buffer = protocol.decode(connection.getSessionKey(), buffer.order(ByteOrder.LITTLE_ENDIAN));
                }
            }

            if(buffer != null) {


                soeMessageRouter.routeMessage(connection, buffer);
            }

        } catch (Exception e) {
            throw new RuntimeException(buffer.toString(), e);
        }
    }

    @Override
    public void sendMessage(SoeUdpConnection connection, ByteBuffer buffer) {

        UdpPacketType packetType = UdpPacketType.values()[buffer.get(1)];

        if (packetType != UdpPacketType.cUdpPacketConnect && packetType != UdpPacketType.cUdpPacketConfirm) {
            buffer = protocol.encode(connection.getSessionKey(), buffer, true);
            protocol.appendCRC(connection.getSessionKey(), buffer, 2);
            buffer.rewind();
        }

        handleOutgoing(buffer, connection.getRemoteAddress());
    }

    public void stop() {
        sendThread.interrupt();
        super.stop();
    }

    private class SendLoop implements Runnable {

        @Override
        public void run() {

            long nextIteration = 0;

            try {
                while (true) {

                    long currentTime = System.currentTimeMillis();

                    if (nextIteration > currentTime) {
                        Thread.sleep(nextIteration - currentTime);
                    }

                    try {

                        nextIteration = currentTime + sendQueueInterval;

                        Set<Object> connectionList = connectionMap.keySet();
                        List<Object> deadClients = new ArrayList<>();

                        for (Object obj : connectionList) {
                            SoeUdpConnection connection = connectionMap.get(obj);

                            if (connection == null || connection.isStale()) {
                                deadClients.add(obj);
                                continue;
                            }

                            List<ByteBuffer> messages = connection.getPendingMessages();

                            for (ByteBuffer message : messages) {
                                sendMessage(connection, message);
                            }
                        }

                        for (Object key : deadClients) {
                            logger.debug("Removing client: " + key);
                            SoeUdpConnection connection = connectionMap.remove(key);
                            connection.setState(ConnectionState.DISCONNECTED);
                        }

                    } catch (Exception e) {
                        logger.error("Unknown", e);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Send thread interrupted", e);
            }
        }

    }
}
