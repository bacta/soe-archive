package com.ocdsoft.bacta.soe.io.udp.game;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.ConnectionServerAgent;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.dispatch.SoeDevelopMessageDispatcher;
import com.ocdsoft.bacta.soe.service.OutgoingConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 *
 */

public class GameServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);

    private final BactaConfiguration configuration;

    private final GameServerState serverState;

    private final ConnectionServerAgent connectionServerAgent;

    private final SoeTransceiver transceiver;

    @Inject
    public GameServer(final BactaConfiguration configuration,
                      final GameServerState serverState,
                      final ConnectionServerAgent connectionServerAgent,
                      final OutgoingConnectionService outgoingConnectionService,
                      final Injector injector,
                      final MetricRegistry metricRegistry,
                      final HealthCheckRegistry healthCheckRegistry) throws UnknownHostException {

        this.configuration = configuration;
        this.serverState = serverState;
        this.connectionServerAgent = connectionServerAgent;

        SoeDevelopMessageDispatcher soeMessageRouter = new SoeDevelopMessageDispatcher(
                injector,
                configuration.getStringCollection("Bacta/GameServer", "swgControllerClasspath")
        );

        transceiver = new SoeTransceiver(
                metricRegistry,
                injector.getInstance(NetworkConfiguration.class),
                InetAddress.getByName(configuration.getString("Bacta/GameServer", "BindIp")),
                configuration.getInt("Bacta/GameServer", "Port"),
                ServerType.GAME,
                soeMessageRouter,
                configuration.getStringCollection("Bacta/GameServer", "TrustedClient"));

        ((GameOutgoingConnectionService)outgoingConnectionService).createConnection = transceiver::createOutgoingConnection;

        soeMessageRouter.load();
    }

    @Override
    public void run() {
        logger.info("Starting");
        try {

            Thread agentThread = new Thread(connectionServerAgent);
            agentThread.setName("Connection Server Agent");
            agentThread.start();

            Thread pingThread = new Thread(new PingServer(
                    InetAddress.getByName(configuration.getString("Bacta/GameServer", "BindIp")),
                    configuration.getIntWithDefault("Bacta/GameServer", "Ping", 44462)));
            pingThread.start();

            serverState.setServerStatus(ServerStatus.UP);
            connectionServerAgent.update();

            // Blocks until stopped
            transceiver.run();

            serverState.setServerStatus(ServerStatus.DOWN);
            connectionServerAgent.update();
            agentThread.interrupt();

        } catch (Exception e) {
            logger.error("Error starting game transceiver", e);
        }
    }

    public void stop() {
        if(transceiver != null) {
            transceiver.stop();
        }
    }
    
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
