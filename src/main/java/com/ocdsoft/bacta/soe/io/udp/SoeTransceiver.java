package com.ocdsoft.bacta.soe.io.udp;

import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.engine.network.io.udp.UdpTransceiver;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kburkhardt on 2/15/14.
 */

public abstract class SoeTransceiver<Connection extends SoeUdpConnection> extends UdpTransceiver<Connection> {

    private final Logger logger = LoggerFactory.getLogger(SoeTransceiver.class);

    private final ServerType serverType;

    private final SoeMessageRouter soeMessageRouter;

    private final SoeProtocol protocol;

    /**
     * This is a reference to the constructor used to create the clients
     */
    protected Constructor<Connection> connectionConstructor;

    /**
     * The structure that holds the "connected" udp clients
     */
    protected final Map<Object, Connection> connectionMap;

    private final Thread sendThread;
    private final int sendQueueInterval;

    public SoeTransceiver(final InetAddress bindAddress,
                          final int port,
                          final ServerType serverType,
                          final Class<Connection> connectionClass,
                          final int sendQueueInterval,
                          final SoeMessageRouter soeMessageRouter,
                          final SoeProtocol soeProtocol) {

        super(bindAddress, port);

        try {

            this.serverType = serverType;
            this.sendQueueInterval = sendQueueInterval;
            connectionConstructor = connectionClass.getConstructor();
            this.soeMessageRouter = soeMessageRouter;
            this.protocol = soeProtocol;

            ResourceBundle bundle = PropertyResourceBundle.getBundle("messageprocessing");
            protocol.setCompression(bundle.getString("Compression").equalsIgnoreCase("true"));

            connectionMap = new ConcurrentHashMap<>();

            sendThread = new Thread(new SendLoop());
            sendThread.start();

        } catch (NoSuchMethodException | SecurityException e) {

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
    protected final Connection createConnection(InetSocketAddress address) throws RuntimeException {
        Connection connection;
        try {
            connection = connectionConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        connection.setRemoteAddress(address);
        return connection;
    }

    @Override
    public final void receiveMessage(InetSocketAddress sender, ByteBuffer buffer) {

        try {

            Connection connection = connectionMap.get(sender);

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
            } else {

                if(connection == null) {
                    logger.debug("Unsolicited Message from " + sender);
                    return;
                }

                buffer = protocol.decode(connection.getSessionKey(), buffer.order(ByteOrder.LITTLE_ENDIAN));

            }

            if(buffer != null) {


                soeMessageRouter.routeMessage(connection, buffer);
            }

        } catch (Exception e) {
            throw new RuntimeException(buffer.toString(), e);
        }
    }

    @Override
    public void sendMessage(Connection connection, ByteBuffer buffer) {

        UdpPacketType packetType = UdpPacketType.values()[buffer.get(1)];

        if (packetType != UdpPacketType.cUdpPacketConfirm) {
            buffer = protocol.encode(connection.getSessionKey(), buffer, true);
            protocol.appendCRC(connection.getSessionKey(), buffer, 2);
            buffer.rewind();
        }

        handleOutgoing(buffer, connection.getRemoteAddress());
    }

    private class SendLoop implements Runnable {

        @Override
        public void run() {

            long nextIteration = 0;

            while (true) {
                try {
                    long currentTime = System.currentTimeMillis();
                    if (nextIteration > currentTime) {

                        try {
                            Thread.sleep(nextIteration - currentTime);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    nextIteration = currentTime + sendQueueInterval;

                    Set<Object> connectionList = connectionMap.keySet();
                    List<Object> deadClients = new ArrayList<>();

                    for (Object obj : connectionList) {
                        Connection connection = connectionMap.get(obj);

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
                        Connection connection = connectionMap.remove(key);
                        connection.setState(ConnectionState.DISCONNECTED);
                    }

                } catch (Exception e) {
                    logger.error("Unknown", e);
                }
            }
        }
    }
}
