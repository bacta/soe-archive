package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.ConnectionServerAgent;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 *
 */

public class GameServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GameServer.class);

    private final BactaConfiguration configuration;

    private final GameServerState serverState;

    private final ConnectionServerAgent connectionServerAgent;

    private final Injector injector;

    private SoeTransceiver transceiver;

    @Inject
    public GameServer(final BactaConfiguration configuration,
                      final GameServerState serverState,
                      final ConnectionServerAgent connectionServerAgent,
                      final Injector injector) {

        this.configuration = configuration;
        this.serverState = serverState;
        this.connectionServerAgent = connectionServerAgent;
        this.injector = injector;
    }

    @Override
    public void run() {
        logger.info("Starting");
        try {

            Thread agentThread = new Thread(connectionServerAgent);
            agentThread.start();

            SoeMessageRouter soeMessageRouter = new SoeMessageRouter(
                    injector,
                    configuration.getString("Bacta/GameServer", "SoeControllerList"),
                    configuration.getString("Bacta/GameServer", "SwgControllerList")
            );

            transceiver = new SoeTransceiver(
                    InetAddress.getByName(configuration.getString("Bacta/GameServer", "BindIp")),
                    configuration.getInt("Bacta/GameServer", "Port"),
                    ServerType.GAME,
                    configuration.getInt("Bacta/GameServer", "SendInterval"),
                    soeMessageRouter,
                    configuration.getStringCollection("Bacta/GameServer", "TrustedClient"));

            Thread pingThread = new Thread(new PingServer(
                    InetAddress.getByName(configuration.getString("Bacta/GameServer", "BindIp")),
                    configuration.getIntWithDefault("Bacta/GameServer", "Ping", 44462),
                    transceiver.getConnectionMap()));
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
}
