package com.ocdsoft.bacta.soe.io.udp;

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

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final ServerType serverType;

    private final SoeMessageRouter soeMessageRouter;

    private SoeProtocol protocol;

    /**
     * This is a reference to the constructor used to create the clients
     */
    protected Constructor<Connection> connectionConstructor;

    /**
     * The structure that holds the "connected" udp clients
     */
    protected final Map<Object, Connection> connections;

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

            connections = new ConcurrentHashMap<>();

            sendThread = new Thread(new SendLoop());
            sendThread.start();

        } catch (NoSuchMethodException | SecurityException e) {

            throw new RuntimeException("Unable to start SOE transceiver", e);
        }
    }

    /**
     * The factory method that creates instances of the {@link com.ocdsoft.bacta.engine.network.client.UdpClient} specified in the {@code Client} parameter
     *
     * @param address {@link java.net.InetSocketAddress of incoming {@code Data} message
     * @return New instance of user specified class {@code Connection}
     * @throws Exception
     * @since 1.0
     */
    protected final Connection createClient(InetSocketAddress address) throws RuntimeException {
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

            Connection connection = connections.get(sender);

            byte zeroByte = buffer.get();
            UdpPacketType packetType = UdpPacketType.values()[buffer.get()];

            if (packetType == UdpPacketType.cUdpPacketConnect) {

                connection = createClient(sender);
                connections.put(sender, connection);

                logger.debug("{} connection from {} now has {} total connected clients.",
                        connection.getClass().getSimpleName(),
                        sender,
                        connections.size());
            } else {

                if (buffer.getShort(0) != 1) {
                    buffer = protocol.decode(connection.getSessionKey(), buffer.order(ByteOrder.LITTLE_ENDIAN));
                }
            }

            if(zeroByte == 0) {
                soeMessageRouter.routeMessage(packetType, connection, buffer);
            } else {
                swgRouter.routeMessage(message.readInt(), client, message);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(Connection connection, ByteBuffer buffer) {

        short op = Short.reverseBytes(buffer.getShort(0));

        if (op > 0x2 && op != 0x4) {
            buffer = protocol.encode(connection.getSessionKey(), buffer, true);
            protocol.appendCRC(connection.getSessionKey(), buffer, 2);
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

                    Set<Object> connectionList = connections.keySet();
                    List<Object> deadClients = new ArrayList<>();

                    for (Object obj : connectionList) {
                        Connection connection = connections.get(obj);

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
                        Connection connection = connections.remove(key);
                        connection.close();
                    }

                } catch (Exception e) {
                    logger.error("Unknown", e);
                }
            }
        }
    }
}
