package com.ocdsoft.bacta.soe.io.udp.game;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Map;

/**
 * Created by kburkhardt on 2/14/14.
 */

public class PingServer implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(PingServer.class);

    private PingTransceiver pingTransceiver;

    public PingServer(InetAddress bindAddress, int port, Map<Object, SoeUdpConnection> connectionMap) {
        pingTransceiver = new PingTransceiver(bindAddress, port, connectionMap);
    }


    @Override
    public void run() {
        logger.info("Starting ping server");

        pingTransceiver.run();

    }
}
