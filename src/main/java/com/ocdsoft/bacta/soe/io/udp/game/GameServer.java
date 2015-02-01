package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 *
 */

public class GameServer implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    @Inject
    private GameTransceiverFactory gameTransceiverFactory;

    @Inject
    private BactaConfiguration configuration;

    @Inject
    private GameServerState serverState;

    @Inject
    private GameServerStatusUpdater gameServerStatusUpdater;

    @Override
    public void run() {
        logger.info("Starting");

        try {

            InetAddress bindAddress = InetAddress.getByName(
                    configuration.getStringWithDefault("Bacta/GameServer", "BindIp", "127.0.0.1"));

            int port = configuration.getIntWithDefault("Bacta/GameServer", "Port", 44463);
            int sendInterval = configuration.getIntWithDefault("Bacta/GameServer", "SendInterval", 100);

            int pingPort = configuration.getIntWithDefault("Bacta/GameServer", "Ping", 44462);

            GameTransceiver transceiver = gameTransceiverFactory.create(
                    bindAddress, port, pingPort, GameConnection.class, sendInterval);

            serverState.setServerStatus(ServerStatus.ONLINE);

            gameServerStatusUpdater.start();
            transceiver.run();

        } catch (Exception e) {
            logger.error("Error starting game transceiver", e);
        }
    }
}
