package com.ocdsoft.bacta.soe.io.udp;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.engine.network.io.udp.UdpTransceiver;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.dispatch.SoeMessageDispatcher;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import org.apache.commons.modeler.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by kburkhardt on 2/15/14.
 */

//TODO: Document
public final class SoeTransceiver extends UdpTransceiver<SoeUdpConnection>  {

    private final static Logger LOGGER = LoggerFactory.getLogger(SoeTransceiver.class);

    private final SoeMessageDispatcher soeMessageDispatcher;

    private final SoeProtocol protocol;

    private final Map<Object, SoeUdpConnection> connectionMap;

    private final Thread sendThread;

    private final NetworkConfiguration networkConfiguration;

    private final Collection<String> whitelistedAddresses;
    private final MBeanServer mBeanServer;
    
    private final Counter incomingMessages;
    private final Counter outgoingMessages;
    private final Timer sendTimer;
    private final Histogram sendQueueSizes;

    // Connection Id generator
    private final Random random;

    private final ServerState serverState;

    @Inject
    public SoeTransceiver(final MetricRegistry metrics,
                          final NetworkConfiguration networkConfiguration,
                          final ServerState serverState,
                          final SoeMessageDispatcher soeMessageDispatcher) {

        super(networkConfiguration.getBindIp(), networkConfiguration.getPort());

        this.networkConfiguration = networkConfiguration;
        this.soeMessageDispatcher = soeMessageDispatcher;
        this.protocol = new SoeProtocol();
        this.whitelistedAddresses = networkConfiguration.getTrustedClients();
        this.random = new Random();
        this.serverState = serverState;

        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();

        protocol.setCompression(networkConfiguration.isCompression());

        connectionMap = new ConcurrentHashMap<>();

        sendThread = new Thread(new SendLoop());
        sendThread.setName(serverState.getServerType().name() + " Send");

        outgoingMessages = metrics.counter(MetricRegistry.name(SoeTransceiver.class, "message", "outgoing"));
        incomingMessages = metrics.counter(MetricRegistry.name(SoeTransceiver.class, "message", "incoming"));
        sendQueueSizes = metrics.histogram(MetricRegistry.name(SoeTransceiver.class, "message", "outgoing-queue"));
        sendTimer = metrics.timer(MetricRegistry.name(SoeTransceiver.class, "message", "send-timer"));
        
        metrics.register(MetricRegistry.name(SoeTransceiver.class, "connections", "active"),
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return getConnectionCount();
                    }
                });

        if(!networkConfiguration.isDisableInstrumentation()) {
            try {
                Registry registry = new Registry();
                registry.setMBeanServer(mBeanServer);
                
                String modelerMetadataFile = "/mbeans-descriptors.xml";

                final InputStream modelerXmlInputStream =
                        SoeTransceiver.class.getResourceAsStream(
                                modelerMetadataFile);

                registry.loadMetadata(modelerXmlInputStream);
                registry.registerComponent(this, "Bacta:type=SoeTransceiver,id=" + serverState.getServerType().name(), null);
                
                //mBeanServer.registerMBean(baseModelMBean, new ObjectName("Bacta:type=SoeTransceiver,id=" + serverType.name()));
            
            } catch (Exception e) {
                LOGGER.error("Unable to register transceiver with mbean server", e);
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
        SoeUdpConnection connection = new SoeUdpConnection(networkConfiguration, address, ConnectionState.ONLINE, null);
        
        try {

            if(whitelistedAddresses != null && whitelistedAddresses.contains(address.getHostString())) {
                connection.addRole(ConnectionRole.WHITELISTED);
                LOGGER.info("Whitelisted address connected: " + address.getHostString());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public final SoeUdpConnection createOutgoingConnection(final InetSocketAddress address, final Consumer<SoeUdpConnection> connectCallback) throws RuntimeException {

        try {
            SoeUdpConnection connection = new SoeUdpConnection(networkConfiguration, address, ConnectionState.LINKDEAD, connectCallback);
            connection.setId(random.nextInt());
            
            if(whitelistedAddresses != null && whitelistedAddresses.contains(connection.getRemoteAddress().getHostString())) {
                connection.addRole(ConnectionRole.WHITELISTED);
                LOGGER.debug("Whitelisted address connected: " + connection.getRemoteAddress().getHostString());
            }

            connectionMap.put(connection.getRemoteAddress(), connection);

            LOGGER.debug("{} connection to {} now has {} total connected clients.",
                    connection.getClass().getSimpleName(),
                    connection.getRemoteAddress(),
                    connectionMap.size());

            if(!networkConfiguration.isDisableInstrumentation()) {
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

                LOGGER.trace("Recieved {}", packetType);

                if (packetType == UdpPacketType.cUdpPacketConnect) {

                    connection = createConnection(sender);
                    connectionMap.put(sender, connection);

                    LOGGER.debug("{} connection from {} now has {} total connected clients.",
                            connection.getClass().getSimpleName(),
                            sender,
                            connectionMap.size());

                } else {

                    if (connection == null) {
                        LOGGER.debug("Unsolicited Message from " + sender + ": " + BufferUtil.bytesToHex(buffer));
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
                soeMessageDispatcher.dispatch(connection, buffer);
            } else {
                LOGGER.warn("Unhandled message {}}", packetType);
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
        LOGGER.info("{} Transceiver started on  {}:{}", serverState.getServerType() ,networkConfiguration.getBindIp(), networkConfiguration.getPort());
        sendThread.start();
        super.run();
    }

    public void stop() {
        LOGGER.info("{} Transceiver started on  {}:{}", serverState.getServerType() ,networkConfiguration.getBindIp(), networkConfiguration.getPort());
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
                    
                    Timer.Context context = sendTimer.time();

                    try {
                        
                        nextIteration = currentTime + networkConfiguration.getNetworkThreadSleepTimeMs();

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
                            if(!networkConfiguration.isDisableInstrumentation()) {
                                try {
                                    mBeanServer.unregisterMBean(connection.getBeanName());
                                } catch (Exception e) {
                                    LOGGER.warn("Unable to unregister MBEAN {}", connection.getBeanName(), e);
                                }
                            }
                            if(networkConfiguration.isReportUdpDisconnects()) {
                                LOGGER.info("Client disconnected: {}  Connection: {}  Reason: ", connection.getRemoteAddress(), connection.getId(), connection.getTerminateReason());
                                LOGGER.info("Clients still connected: {}", connectionMap.size());
                            }
                        }

                    } catch (Exception e) {
                        LOGGER.error("Unknown", e);
                    }
                    context.stop();
                }
            } catch (InterruptedException e) {
                LOGGER.warn("Send thread interrupted", e);
            }
        }

    }
}
