package com.ocdsoft.bacta.soe.io.udp.game;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.ConnectionServerAgent;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.dispatch.SoeMessageDispatcher;
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.service.OutgoingConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * GameServer is the main class which starts the services running
 */
public final class GameServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameServer.class);

    /**
     * Various metadata about the game server
     */
    private final GameServerState serverState;

    /**
     * The ConnectionServerAgent communicates the status of this GameServer to the connection server
     */
    private final ConnectionServerAgent connectionServerAgent;

    /**
     * The transceiver receives and transmits all the messages
     */
    private final SoeTransceiver transceiver;

    /**
     * Ping server helps the client gauge its latency
     */
    private final PingServer pingServer;

    @Inject
    public GameServer(final GameServerState serverState,
                      final SoeTransceiver transceiver,
                      final PingServer pingServer,
                      final OutgoingConnectionService outgoingConnectionService,
                      final ConnectionServerAgent connectionServerAgent) throws UnknownHostException {

        this.serverState = serverState;
        this.transceiver = transceiver;
        this.pingServer = pingServer;
        this.connectionServerAgent = connectionServerAgent;

        // One might consider this a hack, but it allows us to use scope in referencing the method desired without making it public
        ((GameOutgoingConnectionService)outgoingConnectionService).createConnection = transceiver::createOutgoingConnection;
    }

    /**
     * Main method that starts both the
     */
    @Override
    public void run() {
        LOGGER.info("Starting");
        try {

            Thread agentThread = new Thread(connectionServerAgent);
            agentThread.setName("Connection Server Agent");
            agentThread.start();

            Thread pingThread = new Thread(pingServer);
            pingThread.start();

            serverState.setServerStatus(ServerStatus.UP);
            connectionServerAgent.update();

            LOGGER.info("Game Server is running");
            // Blocks until stopped
            transceiver.run();
            serverState.setServerStatus(ServerStatus.DOWN);

            connectionServerAgent.update();
            agentThread.interrupt();

        } catch (Exception e) {
            LOGGER.error("Error starting game transceiver", e);
            serverState.setServerStatus(ServerStatus.DOWN);
        }
    }

    public void stop() {
        if(transceiver != null) {
            transceiver.stop();
        }
    }

    /**
     * GameOutgoingConnectionService uses a function reference to the {@link SoeTransceiver#createOutgoingConnection(InetSocketAddress, Consumer)}
     * method to provide various services with a consistent manner in which to initialize outgoing communication
     */
    @Singleton
    final static public class GameOutgoingConnectionService implements OutgoingConnectionService {

        private BiFunction<InetSocketAddress, Consumer<SoeUdpConnection>, SoeUdpConnection> createConnection;

        @Override
        public SoeUdpConnection createOutgoingConnection(final InetSocketAddress address, final Consumer<SoeUdpConnection> connectCallback) {
            if(createConnection == null) return null;
            
            return createConnection.apply(address, connectCallback);
        }
    }
}
