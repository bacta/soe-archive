package com.ocdsoft.bacta.soe.io.udp;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.engine.network.io.udp.UdpTransceiver;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.apache.commons.modeler.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by kburkhardt on 2/15/14.
 */

public final class SoeTransceiver extends UdpTransceiver<SoeUdpConnection>  {

    private final static Logger logger = LoggerFactory.getLogger(SoeTransceiver.class);

    private final SoeMessageRouter soeMessageRouter;

    private final SoeProtocol protocol;

    private final Map<Object, SoeUdpConnection> connectionMap;

    private final Thread sendThread;

    private final NetworkConfiguration configuration;

    private final Collection<String> whitelistedAddresses;
    private final MBeanServer mBeanServer;
    
    private final Counter incomingMessages;
    private final Counter outgoingMessages;
    private final Histogram sendQueueSizes;

    // Connection Id generator
    private final Random random;

    public SoeTransceiver() {
        this(null, null, null, 0, null, null, null);
    }
    
    public SoeTransceiver(final MetricRegistry metrics,
                          final NetworkConfiguration configuration,
                          final InetAddress bindAddress,
                          final int port,
                          final ServerType serverType,
                          final SoeMessageRouter soeMessageRouter,
                          final Collection<String> whitelistedAddresses) {

        super(bindAddress, port);

        this.configuration = configuration;
        this.soeMessageRouter = soeMessageRouter;
        this.protocol = new SoeProtocol();
        this.whitelistedAddresses = whitelistedAddresses;
        this.random = new Random();

        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();

        protocol.setCompression(configuration.isCompression());

        connectionMap = new ConcurrentHashMap<>();

        sendThread = new Thread(new SendLoop());
        sendThread.setName(serverType.name() + " Send Thread");

        outgoingMessages = metrics.counter(MetricRegistry.name(SoeTransceiver.class, "message", "outgoing"));
        incomingMessages = metrics.counter(MetricRegistry.name(SoeTransceiver.class, "message", "incoming"));
        sendQueueSizes = metrics.histogram(MetricRegistry.name("Bacta", SoeTransceiver.class.getSimpleName(), "outgoing-queue"));

        metrics.register(MetricRegistry.name(SoeTransceiver.class, "connections", "active"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return getConnectionCount();
                    }
                });

        if(!configuration.isDisableInstrumentation()) {
            try {
                Registry registry = new Registry();
                registry.setMBeanServer(mBeanServer);
                
                String modelerMetadataFile = "/mbeans-descriptors.xml";

                final InputStream modelerXmlInputStream =
                        SoeTransceiver.class.getResourceAsStream(
                                modelerMetadataFile);

                registry.loadMetadata(modelerXmlInputStream);
                registry.registerComponent(this, "Bacta:type=SoeTransceiver,id=" + serverType.name(), null);
                
                //mBeanServer.registerMBean(baseModelMBean, new ObjectName("Bacta:type=SoeTransceiver,id=" + serverType.name()));
            
            } catch (Exception e) {
                logger.error("Unable to register transceiver with mbean server", e);
            }
        }
    }
    
    public long getIncomingMessageCount() {
        return incomingMessages.getCount();
    }
    
    public long getOutgoingMessageCount() {
        return outgoingMessages.getCount();
    }
    
    public double getAverageSendQueueSize() {
        return sendQueueSizes.getSnapshot().getMean();
    }

    public int getConnectionCount() {
        return connectionMap.size();
    }
    
    /**
     * The factory method that creates instances of the {@link com.ocdsoft.bacta.engine.network.client.UdpConnection} specified in the {@code Client} parameter
     *
     * @param address {@link java.net.InetSocketAddress of incoming {@code Data} message
     * @return New instance of user specified class {@code Connection}
     * @throws Exception
     * @since 1.0
     */
    private final SoeUdpConnection createConnection(final InetSocketAddress address) throws RuntimeException {
        SoeUdpConnection connection = new SoeUdpConnection(configuration, address, ConnectionState.ONLINE, null);
        
        try {

            if(whitelistedAddresses != null && whitelistedAddresses.contains(address.getHostString())) {
                connection.addRole(ConnectionRole.WHITELISTED);
                logger.info("Whitelisted address connected: " + address.getHostString());
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public final SoeUdpConnection createOutgoingConnection(final InetSocketAddress address, final Consumer<SoeUdpConnection> connectCallback) throws RuntimeException {

        try {
            SoeUdpConnection connection = new SoeUdpConnection(configuration, address, ConnectionState.LINKDEAD, connectCallback);
            connection.setId(random.nextInt());
            
            if(whitelistedAddresses != null && whitelistedAddresses.contains(connection.getRemoteAddress().getHostString())) {
                connection.addRole(ConnectionRole.WHITELISTED);
                logger.debug("Whitelisted address connected: " + connection.getRemoteAddress().getHostString());
            }

            connectionMap.put(connection.getRemoteAddress(), connection);

            logger.debug("{} connection to {} now has {} total connected clients.",
                    connection.getClass().getSimpleName(),
                    connection.getRemoteAddress(),
                    connectionMap.size());

            if(!configuration.isDisableInstrumentation()) {
                mBeanServer.registerMBean(connection, connection.getBeanName());
            }
            
            return connection;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void receiveMessage(InetSocketAddress sender, ByteBuffer buffer) {

        try {
            incomingMessages.inc();
            SoeUdpConnection connection = connectionMap.get(sender);
            UdpPacketType packetType;
            
            byte type = buffer.get(1);
            if(type >= 0 && type <= 0x1E) {

                packetType = UdpPacketType.values()[type];

                if (packetType == UdpPacketType.cUdpPacketConnect) {

                    connection = createConnection(sender);
                    connectionMap.put(sender, connection);

                    logger.debug("{} connection from {} now has {} total connected clients.",
                            connection.getClass().getSimpleName(),
                            sender,
                            connectionMap.size());

                } else {

                    if (connection == null) {
                        logger.debug("Unsolicited Message from " + sender + ": " + BufferUtil.bytesToHex(buffer));
                        return;
                    }


                }
            } else {
                packetType = UdpPacketType.cUdpPacketZeroEscape;
            }

            if (packetType != UdpPacketType.cUdpPacketConnect && packetType != UdpPacketType.cUdpPacketConfirm) {
                buffer = protocol.decode(connection.getConfiguration().getEncryptCode(), buffer.order(ByteOrder.LITTLE_ENDIAN));
            }

            if(buffer != null) {
                connection.increaseProtocolMessageReceived();
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
            buffer = protocol.encode(connection.getConfiguration().getEncryptCode(), buffer, true);
            protocol.appendCRC(connection.getConfiguration().getEncryptCode(), buffer, 2);
            buffer.rewind();
        }

        outgoingMessages.inc();
        handleOutgoing(buffer, connection.getRemoteAddress());
    }

    @Override
    public final void run() {
        sendThread.start();
        super.run();
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

                while(ctx == null) {
                    Thread.sleep(100);
                }
                
                while (true) {

                    long currentTime = System.currentTimeMillis();

                    if (nextIteration > currentTime) {
                        Thread.sleep(nextIteration - currentTime);
                    }

                    try {

                        nextIteration = currentTime + configuration.getNetworkThreadSleepTimeMs();

                        Set<Object> connectionList = connectionMap.keySet();
                        List<Object> deadClients = new ArrayList<>();

                        for (Object obj : connectionList) {
                            SoeUdpConnection connection = connectionMap.get(obj);

                            if (connection == null || connection.getState() == ConnectionState.DISCONNECTED) {
                                deadClients.add(obj);
                                continue;
                            }

                            List<ByteBuffer> messages = connection.getPendingMessages();
                            if(messages.size() > 0) {
                                sendQueueSizes.update(messages.size());
                            }
                            
                            for (ByteBuffer message : messages) {
                                sendMessage(connection, message);
                            }
                        }

                        for (Object key : deadClients) {
                            SoeUdpConnection connection = connectionMap.remove(key);
                            if(!configuration.isDisableInstrumentation()) {
                                mBeanServer.unregisterMBean(connection.getBeanName());
                            }
                            if(configuration.isReportUdpDisconnects()) {
                                logger.info("Client disconnected: " + connection.getRemoteAddress() + " Connection: " + connection.getId() + " Reason: " + connection.getTerminateReason());
                            }
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
