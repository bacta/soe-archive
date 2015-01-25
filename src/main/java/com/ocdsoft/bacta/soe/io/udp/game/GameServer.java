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

    @Override
    public void run() {
        logger.info("Starting");
        try {

            InetAddress bindAddress = InetAddress.getByName(
                    configuration.getString("Bacta/GameServer", "BindIp"));

            int port = configuration.getInt("Bacta/GameServer", "Port");
            int sendInterval = configuration.getInt("Bacta/GameServer", "SendInterval");

            int pingPort = configuration.getIntWithDefault("Bacta/GameServer", "Ping", 44462);

            GameTransceiver transceiver = gameTransceiverFactory.create(bindAddress, port, pingPort, GameConnection.class, sendInterval);

            serverState.setServerStatus(ServerStatus.UP);
            transceiver.run();

        } catch (Exception e) {
            logger.error("Error starting game transceiver", e);
        }
    }
}
