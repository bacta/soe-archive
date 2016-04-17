package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Created by kburkhardt on 2/14/14.
 */

@Singleton
public final class PingServer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(PingServer.class);

    private final PingTransceiver pingTransceiver;

    @Inject
    public PingServer(final PingTransceiver pingTransceiver) {
        this.pingTransceiver = pingTransceiver;
    }

    @Override
    public void run() {
        LOGGER.info("Starting ping server");
        pingTransceiver.run();
        LOGGER.info("Stopping ping server");
    }
}
