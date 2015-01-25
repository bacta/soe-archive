package com.ocdsoft.bacta.soe.io.udp.login;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by kburkhardt on 2/14/14.
 */

public class LoginServer implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    @Inject
    private LoginTransceiverFactory loginTransceiverFactory;

    @Inject
    private BactaConfiguration configuration;

    @Inject
    private LoginServerState serverState;

    @Override
    public void run() {
        logger.info("Starting");

        InetAddress bindAddress;
        try {
            bindAddress = InetAddress.getByName(configuration.getString("Bacta/LoginServer", "BindIp"));
        } catch (UnknownHostException e) {
            bindAddress = null;
            logger.error("Unknown Host", e);
        }
        int port = configuration.getInt("Bacta/LoginServer", "Port");
        int sendInterval = configuration.getInt("Bacta/LoginServer", "SendInterval");

        serverState.setServerStatus(ServerStatus.LOADING);

        LoginTransceiver transceiver = loginTransceiverFactory.create(bindAddress, port, LoginConnection.class, sendInterval);

        logger.info("Listening on port " + port);
        serverState.setServerStatus(ServerStatus.UP);
        transceiver.run();
    }
}
