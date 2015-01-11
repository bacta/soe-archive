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
            bindAddress = InetAddress.getByName(configuration.getStringWithDefault("Bacta/LoginServer", "BindIp", "127.0.0.1"));
        } catch (UnknownHostException e) {
            bindAddress = null;
            logger.error("Unknown Host", e);
        }
        int port = configuration.getIntWithDefault("Bacta/LoginServer", "Port", 44453);
        int sendInterval = configuration.getIntWithDefault("Bacta/LoginServer", "SendInterval", 100);

        serverState.setServerStatus(ServerStatus.LOADING);

        LoginTransceiver transceiver = loginTransceiverFactory.create(bindAddress, port, LoginConnection.class, sendInterval);

        logger.info("Listening on port " + port);
        serverState.setServerStatus(ServerStatus.ONLINE);
        transceiver.run();
    }
}
